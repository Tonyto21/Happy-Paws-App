const fs = require('fs');
const path = require('path');
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require('@firebase/rules-unit-testing');

const PROJECT_ID = 'demo-happypaws-liberia';

async function runSecurityTests() {
  console.log("=================================================");
  console.log("   HAPPY PAWS LIBERIA FIRESTORE SECURITY TESTS   ");
  console.log("=================================================\n");

  const rulesContent = fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8');

  let testEnv;
  const firestorePort = parseInt(process.env.FIRESTORE_PORT || '8898', 10);
  const hubPort = parseInt(process.env.HUB_PORT || '4400', 10);
  try {
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      hub: {
        host: '127.0.0.1',
        port: hubPort,
      },
      firestore: {
        host: '127.0.0.1',
        port: firestorePort,
      },
    });
  } catch (err) {
    console.error("Failed to connect to Firestore emulator:", err.message);
    console.error(err.stack);
    process.exit(1);
  }

  let passedCount = 0;
  let totalCount = 0;

  async function test(description, testFn) {
    totalCount++;
    try {
      await testFn();
      console.log(`[PASS] Test #${totalCount}: ${description}`);
      passedCount++;
    } catch (e) {
      console.error(`[FAIL] Test #${totalCount}: ${description}`);
      console.error(`       Error: ${e.message}\n`);
    }
  }

  // Setup initial mock data using withSecurityRulesDisabled
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    
    // 1. Staff roles
    await db.doc('staff_roles/super_admin_uid').set({
      role: 'super_admin',
      active: true,
      email: 'admin@happypaws.org'
    });
    await db.doc('staff_roles/vet_uid').set({
      role: 'veterinarian',
      active: true,
      email: 'dr.sackor@happypaws.org'
    });
    await db.doc('staff_roles/reception_uid').set({
      role: 'receptionist',
      active: true,
      email: 'frontdesk@happypaws.org'
    });

    // 2. Clients
    await db.doc('clients/client_owner_1').set({
      ownerAuthId: 'owner_1_uid',
      fullName: 'Alice Johnson',
      phone: '0771234567'
    });
    await db.doc('clients/client_owner_2').set({
      ownerAuthId: 'owner_2_uid',
      fullName: 'Bob Smith',
      phone: '0779876543'
    });

    // 3. Pets
    await db.doc('pets/pet_1').set({
      clientId: 'client_owner_1',
      name: 'Bella',
      species: 'Dog'
    });
    await db.doc('pets/pet_2').set({
      clientId: 'client_owner_2',
      name: 'Simba',
      species: 'Cat'
    });

    // 4. Consultations
    await db.doc('consultations/consult_draft_1').set({
      petId: 'pet_1',
      isFinalized: false,
      isOwnerVisible: true,
      diagnosis: 'Routine Health Check',
      internalNotes: 'Internal confidential observation'
    });
    await db.doc('consultations/consult_final_1').set({
      petId: 'pet_1',
      isFinalized: true,
      isOwnerVisible: true,
      diagnosis: 'Confirmed Parvovirus - Treatment initiated',
      internalNotes: 'Staff only clinical prognosis'
    });
    await db.doc('consultations/consult_internal_only').set({
      petId: 'pet_1',
      isFinalized: true,
      isOwnerVisible: false,
      diagnosis: 'Internal behavioral evaluation'
    });

    // 5. Inventory
    await db.doc('inventory_items/sku_rabies_01').set({
      name: 'Rabies Vaccine Vial',
      sku: 'VAC-RAB-01',
      quantityOnHand: 45
    });

    // 6. Invoices & Payments
    await db.doc('invoices/inv_101').set({
      clientId: 'client_owner_1',
      totalAmount: 50.0,
      amountPaid: 50.0,
      status: 'Paid'
    });
    await db.doc('payments/pay_501').set({
      invoiceId: 'inv_101',
      clientId: 'client_owner_1',
      amount: 50.0,
      paymentMethod: 'Mobile Money'
    });
  });

  const owner1Context = testEnv.authenticatedContext('owner_1_uid', { email: 'alice@example.com' });
  const owner2Context = testEnv.authenticatedContext('owner_2_uid', { email: 'bob@example.com' });
  const vetContext = testEnv.authenticatedContext('vet_uid', { email: 'dr.sackor@happypaws.org' });
  const receptionContext = testEnv.authenticatedContext('reception_uid', { email: 'frontdesk@happypaws.org' });
  const superAdminContext = testEnv.authenticatedContext('super_admin_uid', { email: 'admin@happypaws.org' });
  const unauthContext = testEnv.unauthenticatedContext();

  // Test 1: Pet Owner CAN access their own client and pet
  await test("Pet Owner CAN read their own client profile and pet", async () => {
    const db = owner1Context.firestore();
    await assertSucceeds(db.doc('clients/client_owner_1').get());
    await assertSucceeds(db.doc('pets/pet_1').get());
  });

  // Test 2: Pet Owner CANNOT access another owner's client or pet
  await test("Pet Owner CANNOT access another owner's client or pet", async () => {
    const db = owner1Context.firestore();
    await assertFails(db.doc('clients/client_owner_2').get());
    await assertFails(db.doc('pets/pet_2').get());
  });

  // Test 3: Pet Owner CANNOT access another owner's consultations
  await test("Pet Owner CANNOT access another owner's consultations", async () => {
    const db = owner2Context.firestore();
    await assertFails(db.doc('consultations/consult_draft_1').get());
  });

  // Test 4: Pet Owner CANNOT read internal-only clinical information (isOwnerVisible == false)
  await test("Pet Owner CANNOT read internal-only clinical records (isOwnerVisible == false)", async () => {
    const db = owner1Context.firestore();
    await assertFails(db.doc('consultations/consult_internal_only').get());
  });

  // Test 5: Receptionist CANNOT perform veterinarian-only clinical operations (create consultation)
  await test("Receptionist CANNOT create consultations or prescriptions", async () => {
    const db = receptionContext.firestore();
    await assertFails(db.collection('consultations').add({
      petId: 'pet_1',
      diagnosis: 'Illegal receptionist note',
      isFinalized: false
    }));
    await assertFails(db.collection('vaccinations').add({
      petId: 'pet_1',
      vaccineName: 'Rabies',
      administeredDate: '2026-09-28'
    }));
  });

  // Test 6: Veterinarian CANNOT manage staff roles
  await test("Veterinarian CANNOT manage staff roles", async () => {
    const db = vetContext.firestore();
    await assertFails(db.doc('staff_roles/new_fake_admin').set({
      role: 'super_admin',
      active: true
    }));
  });

  // Test 7: Unauthorized user / Pet Owner CANNOT modify inventory
  await test("Pet Owner / Unauthorized user CANNOT modify inventory", async () => {
    const db = owner1Context.firestore();
    await assertFails(db.doc('inventory_items/sku_rabies_01').update({
      quantityOnHand: 0
    }));
  });

  // Test 8: Inventory CANNOT be updated to negative quantity
  await test("Staff CANNOT update inventory to negative quantityOnHand", async () => {
    const db = vetContext.firestore();
    await assertFails(db.doc('inventory_items/sku_rabies_01').update({
      quantityOnHand: -5
    }));
  });

  // Test 9: Finalized consultation CANNOT be modified or deleted
  await test("Finalized consultation CANNOT be modified by veterinarian or staff", async () => {
    const db = vetContext.firestore();
    await assertFails(db.doc('consultations/consult_final_1').update({
      diagnosis: 'Altered historical clinical notes'
    }));
    await assertFails(db.doc('consultations/consult_final_1').delete());
  });

  // Test 10: Finalized consultation CANNOT be deleted even by Super Admin
  await test("Finalized consultation CANNOT be deleted by Super Admin", async () => {
    const db = superAdminContext.firestore();
    await assertFails(db.doc('consultations/consult_final_1').delete());
  });

  // Test 11: Payment records CANNOT be edited or deleted (strict append-only)
  await test("Payment records CANNOT be edited or deleted by ANY client or role", async () => {
    const db = superAdminContext.firestore();
    await assertFails(db.doc('payments/pay_501').update({
      amount: 10.0
    }));
    await assertFails(db.doc('payments/pay_501').delete());
  });

  // Test 12: Ordinary user CANNOT self-assign Super Admin or Clinic Owner privileges
  await test("Ordinary user CANNOT self-assign staff roles or elevate permissions", async () => {
    const db = owner1Context.firestore();
    await assertFails(db.doc('staff_roles/owner_1_uid').set({
      role: 'super_admin',
      active: true
    }));
  });

  // Test 13: Least-privilege on staff_roles: Ordinary user cannot view other staff roles
  await test("Ordinary user CANNOT read other staff role documents", async () => {
    const db = owner1Context.firestore();
    await assertFails(db.doc('staff_roles/super_admin_uid').get());
    await assertFails(db.doc('staff_roles/vet_uid').get());
  });

  // Test 14: Super Admin CAN create staff invitation
  await test("Super Admin CAN create staff invitation", async () => {
    const db = superAdminContext.firestore();
    await assertSucceeds(db.collection('staff_invitations').add({
      email: 'newvet@happypaws.org',
      role: 'veterinarian',
      invitedAt: Date.now()
    }));
  });

  console.log(`\n=================================================`);
  console.log(`RESULTS: ${passedCount} / ${totalCount} Security Rules Tests Passed`);
  console.log(`=================================================`);

  await testEnv.cleanup();
  if (passedCount === totalCount) {
    process.exit(0);
  } else {
    process.exit(1);
  }
}

runSecurityTests();
