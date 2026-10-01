const { spawn } = require('child_process');
const net = require('net');
const fs = require('fs');
const path = require('path');

function getFreePort() {
  return new Promise((resolve) => {
    const s = net.createServer();
    s.listen(0, () => {
      const port = s.address().port;
      s.close(() => resolve(port));
    });
  });
}

function waitForPort(port, timeoutMs = 15000) {
  const start = Date.now();
  return new Promise((resolve, reject) => {
    function tryConnect() {
      const sock = new net.Socket();
      sock.setTimeout(1000);
      sock.on('connect', () => {
        sock.destroy();
        resolve();
      });
      sock.on('error', () => {
        sock.destroy();
        if (Date.now() - start > timeoutMs) {
          reject(new Error(`Timeout waiting for port ${port}`));
        } else {
          setTimeout(tryConnect, 250);
        }
      });
      sock.on('timeout', () => {
        sock.destroy();
        if (Date.now() - start > timeoutMs) {
          reject(new Error(`Timeout waiting for port ${port}`));
        } else {
          setTimeout(tryConnect, 250);
        }
      });
      sock.connect(port, '127.0.0.1');
    }
    tryConnect();
  });
}

async function main() {
  const firestorePort = await getFreePort();
  const hubPort = await getFreePort();
  const loggingPort = await getFreePort();

  console.log(`Allocated ports: Firestore=${firestorePort}, Hub=${hubPort}, Logging=${loggingPort}`);

  // Write temporary firebase.json
  const firebaseConfig = {
    firestore: {
      rules: path.resolve(__dirname, '../firestore.rules')
    },
    emulators: {
      hub: { port: hubPort },
      logging: { port: loggingPort },
      firestore: { port: firestorePort },
      ui: { enabled: false }
    }
  };

  const configPath = path.resolve(__dirname, 'firebase.dynamic.json');
  fs.writeFileSync(configPath, JSON.stringify(firebaseConfig, null, 2));

  console.log("Starting Firestore emulator...");
  const emu = spawn('firebase', [
    'emulators:start',
    '--only', 'firestore',
    '--config', configPath
  ], {
    cwd: __dirname,
    stdio: ['ignore', 'pipe', 'pipe']
  });

  emu.stdout.on('data', (d) => process.stdout.write(`[emu] ${d}`));
  emu.stderr.on('data', (d) => process.stderr.write(`[emu:err] ${d}`));

  try {
    await waitForPort(firestorePort, 20000);
    console.log(`\nFirestore emulator is READY on port ${firestorePort}!\n`);

    // Run tests
    console.log("Running security rules tests...");
    const testProc = spawn('node', ['test_security_rules.js'], {
      cwd: __dirname,
      env: {
        ...process.env,
        HUB_PORT: String(hubPort),
        FIRESTORE_PORT: String(firestorePort),
        FIRESTORE_EMULATOR_HOST: `127.0.0.1:${firestorePort}`,
        FIREBASE_EMULATOR_HUB: `127.0.0.1:${hubPort}`
      },
      stdio: 'inherit'
    });

    await new Promise((resolve, reject) => {
      testProc.on('exit', (code) => {
        if (code === 0) resolve();
        else reject(new Error(`Test process exited with code ${code}`));
      });
    });

    console.log("\nALL TESTS COMPLETED SUCCESSFULLY.");
  } finally {
    console.log("Shutting down Firestore emulator...");
    emu.kill('SIGINT');
    setTimeout(() => emu.kill('SIGKILL'), 3000);
  }
}

main().catch((err) => {
  console.error("Runner failed:", err.message);
  process.exit(1);
});
