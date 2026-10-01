package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.*

object PdfPrintHelper {

    fun printPassport(
        context: Context,
        pet: PetEntity,
        owner: ClientEntity?,
        vaccines: List<VaccinationEntity>,
        dewormings: List<DewormingEntity>,
        clinicSettings: ClinicSettingsEntity?
    ) {
        val clinicName = clinicSettings?.clinicName ?: "Happy Paws Liberia"
        val subtitle = clinicSettings?.subTitle ?: "Rescue Center & Veterinary Clinic"
        val phone = clinicSettings?.vetPhoneNumber ?: "0881479329"
        val address = clinicSettings?.address ?: "Tubman Blvd, Congo Town, Monrovia"
        val email = clinicSettings?.email ?: "care@happypawsliberia.org"

        val vaccinesHtml = if (vaccines.isEmpty()) {
            "<tr><td colspan='4' style='text-align:center; padding: 12px; color: #777;'>No vaccination records recorded yet.</td></tr>"
        } else {
            vaccines.joinToString("") { v ->
                "<tr>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'><b>${v.vaccineName}</b><br><small>Lot: ${v.batchLotNumber.ifEmpty { "N/A" }}</small></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>${v.administeredDate}</td>" +
                "<td style='padding: 8px; border: 1px solid #ddd; color: #C25E3E;'><b>${v.nextDueDate}</b></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>${v.veterinarian}</td>" +
                "</tr>"
            }
        }

        val dewormingHtml = if (dewormings.isEmpty()) {
            "<tr><td colspan='4' style='text-align:center; padding: 12px; color: #777;'>No deworming records recorded yet.</td></tr>"
        } else {
            dewormings.joinToString("") { d ->
                "<tr>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'><b>${d.productName}</b><br><small>Dose: ${d.dosage}</small></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>${d.administeredDate}</td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>${d.nextDueDate}</td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>${d.veterinarian}</td>" +
                "</tr>"
            }
        }

        val htmlDocument = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Official Pet Health Passport - ${pet.name}</title>
                <style>
                    body { font-family: 'Georgia', serif; margin: 24px; color: #1E2124; background: #FFF; }
                    .header { text-align: center; border-bottom: 2px solid #1E2124; padding-bottom: 12px; margin-bottom: 20px; }
                    .clinic-title { font-size: 24px; font-weight: bold; letter-spacing: 1px; margin: 0; }
                    .clinic-sub { font-size: 13px; color: #C25E3E; font-weight: bold; letter-spacing: 2px; margin-top: 4px; }
                    .contact { font-size: 12px; color: #666; margin-top: 6px; }
                    .section-title { font-size: 16px; font-weight: bold; margin-top: 18px; margin-bottom: 8px; color: #1E2124; border-bottom: 1px solid #C25E3E; padding-bottom: 4px; }
                    .grid { display: flex; justify-content: space-between; margin-bottom: 16px; }
                    .card { flex: 1; background: #FFFDF9; border: 1px solid #EAE3D6; border-radius: 8px; padding: 12px; margin: 0 4px; font-size: 13px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 12px; }
                    th { background: #F8F5EE; padding: 8px; border: 1px solid #ddd; text-align: left; }
                    .footer { text-align: center; margin-top: 32px; font-size: 11px; color: #888; border-top: 1px solid #eee; padding-top: 12px; }
                    .seal { border: 2px dashed #436B4F; color: #436B4F; padding: 8px 16px; display: inline-block; font-weight: bold; border-radius: 6px; margin-top: 16px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="clinic-title">${clinicName.uppercase()}</h1>
                    <div class="clinic-sub">${subtitle.uppercase()}</div>
                    <div class="contact">${address} • VET HOTLINE: ${phone} • ${email}</div>
                </div>

                <div class="grid">
                    <div class="card">
                        <b>PATIENT IDENTIFICATION</b><br>
                        Name: <b>${pet.name}</b><br>
                        Species: ${pet.species} | Breed: ${pet.breed}<br>
                        Sex: ${pet.sex} | Weight: ${pet.weightKg} kg<br>
                        Microchip: ${pet.microchipId.ifEmpty { "Not registered" }}<br>
                        Status: ${if (pet.isRescuePet) "Rescue Companion" else "Family Companion"}
                    </div>
                    <div class="card">
                        <b>GUARDIAN / PET PARENT</b><br>
                        Name: <b>${owner?.fullName ?: "N/A"}</b><br>
                        Phone: ${owner?.phone ?: "N/A"}<br>
                        Address: ${owner?.address ?: "Monrovia, Liberia"}<br>
                        Passport Issue Date: 2026-09-28
                    </div>
                </div>

                <div class="section-title">IMMUNIZATION RECORD & VACCINATIONS</div>
                <table>
                    <thead>
                        <tr>
                            <th>Vaccine / Lot</th>
                            <th>Administered</th>
                            <th>Next Due</th>
                            <th>Veterinarian</th>
                        </tr>
                    </thead>
                    <tbody>
                        $vaccinesHtml
                    </tbody>
                </table>

                <div class="section-title">DEWORMING & PARASITE PREVENTION</div>
                <table>
                    <thead>
                        <tr>
                            <th>Product / Dosage</th>
                            <th>Administered</th>
                            <th>Next Due</th>
                            <th>Veterinarian</th>
                        </tr>
                    </thead>
                    <tbody>
                        $dewormingHtml
                    </tbody>
                </table>

                <div style="text-align: right;">
                    <div class="seal">OFFICIALLY CERTIFIED • HAPPY PAWS VETERINARY CLINIC</div>
                </div>

                <div class="footer">
                    This document is an authoritative pet health record issued by ${clinicName}. Emergency care available 24/7 at ${phone}.
                </div>
            </body>
            </html>
        """.trimIndent()

        doPrintHtml(context, "Pet_Passport_${pet.name}", htmlDocument)
    }

    fun printInvoice(
        context: Context,
        invoice: InvoiceEntity,
        client: ClientEntity?,
        pet: PetEntity?,
        payments: List<PaymentEntity>,
        clinicSettings: ClinicSettingsEntity?
    ) {
        val clinicName = clinicSettings?.clinicName ?: "Happy Paws Liberia"
        val subtitle = clinicSettings?.subTitle ?: "Rescue Center & Veterinary Clinic"
        val phone = clinicSettings?.vetPhoneNumber ?: "0881479329"
        val address = clinicSettings?.address ?: "Tubman Blvd, Congo Town, Monrovia"
        val email = clinicSettings?.email ?: "billing@happypawsliberia.org"

        val paymentsHtml = if (payments.isEmpty()) {
            "<tr><td colspan='4' style='text-align:center; padding: 8px; color: #888;'>No payments recorded yet.</td></tr>"
        } else {
            payments.joinToString("") { p ->
                "<tr>" +
                "<td style='padding: 6px; border: 1px solid #eee;'>${p.paymentDate}</td>" +
                "<td style='padding: 6px; border: 1px solid #eee;'>${p.paymentMethod}</td>" +
                "<td style='padding: 6px; border: 1px solid #eee;'>${p.reference.ifEmpty { "Cash receipt" }}</td>" +
                "<td style='padding: 6px; border: 1px solid #eee; text-align: right;'><b>$${"%.2f".format(p.amount)}</b></td>" +
                "</tr>"
            }
        }

        val balance = invoice.totalAmount - invoice.amountPaid

        val htmlDocument = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Invoice #${invoice.invoiceNumber}</title>
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 30px; color: #222; }
                    .header { display: flex; justify-content: space-between; border-bottom: 2px solid #C25E3E; padding-bottom: 12px; margin-bottom: 24px; }
                    .title { font-size: 22px; font-weight: bold; color: #1E2124; }
                    .invoice-badge { text-align: right; }
                    .meta { display: flex; justify-content: space-between; margin-bottom: 24px; font-size: 13px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                    th { background: #F8F5EE; padding: 10px; border: 1px solid #ddd; text-align: left; font-size: 12px; }
                    td { padding: 8px 10px; border: 1px solid #ddd; font-size: 13px; }
                    .totals { width: 40%; margin-left: auto; margin-top: 16px; font-size: 14px; }
                    .totals td { padding: 6px; border: none; }
                    .total-due { font-size: 16px; font-weight: bold; color: #C25E3E; }
                    .footer { text-align: center; margin-top: 40px; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 16px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <div class="title">${clinicName.uppercase()}</div>
                        <div style="font-size: 12px; color: #C25E3E; font-weight: bold;">${subtitle}</div>
                        <div style="font-size: 12px; color: #666; margin-top: 4px;">${address} • Phone: ${phone} • ${email}</div>
                    </div>
                    <div class="invoice-badge">
                        <div style="font-size: 20px; font-weight: bold; color: #C25E3E;">INVOICE</div>
                        <div style="font-size: 13px;">#${invoice.invoiceNumber}</div>
                        <div style="font-size: 12px; color: #666;">Date: ${invoice.date}</div>
                    </div>
                </div>

                <div class="meta">
                    <div>
                        <b>BILLED TO:</b><br>
                        ${client?.fullName ?: "Customer"}<br>
                        Phone: ${client?.phone ?: "N/A"}<br>
                        Patient: <b>${pet?.name ?: "Pet"}</b> (${pet?.species ?: "Companion"})
                    </div>
                    <div style="text-align: right;">
                        <b>PAYMENT STATUS:</b><br>
                        <span style="display: inline-block; padding: 4px 8px; border-radius: 4px; font-weight: bold; background: #EEF5EE; color: #436B4F;">${invoice.status.uppercase()}</span><br>
                        Due Date: ${invoice.dueDate}
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Description of Services & Pharmacy</th>
                            <th style="text-align: right; width: 120px;">Amount (USD)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>${invoice.itemsSummary}</td>
                            <td style="text-align: right;"><b>$${"%.2f".format(invoice.totalAmount)}</b></td>
                        </tr>
                    </tbody>
                </table>

                <table class="totals">
                    <tr>
                        <td>Subtotal:</td>
                        <td style="text-align: right;">$${"%.2f".format(invoice.totalAmount)}</td>
                    </tr>
                    <tr>
                        <td>Amount Paid:</td>
                        <td style="text-align: right; color: #436B4F;">-$${"%.2f".format(invoice.amountPaid)}</td>
                    </tr>
                    <tr class="total-due">
                        <td><b>Balance Due:</b></td>
                        <td style="text-align: right;"><b>$${"%.2f".format(balance)}</b></td>
                    </tr>
                </table>

                <div style="margin-top: 24px;">
                    <b>Payment History:</b>
                    <table>
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Method</th>
                                <th>Reference</th>
                                <th style="text-align: right;">Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            $paymentsHtml
                        </tbody>
                    </table>
                </div>

                <div class="footer">
                    Thank you for supporting Happy Paws Liberia! We accept Mobile Money (MTN & Orange) and Cash.<br>
                    Questions? Contact us at ${phone} or ${email}.
                </div>
            </body>
            </html>
        """.trimIndent()

        doPrintHtml(context, "Invoice_${invoice.invoiceNumber}", htmlDocument)
    }

    private fun doPrintHtml(context: Context, jobName: String, htmlContent: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }
}
