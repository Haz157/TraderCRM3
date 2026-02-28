package apps.farm.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import apps.farm.R
import apps.farm.data.model.Customer
import apps.farm.data.model.CustomerTransaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerStatementPdfGenerator(private val context: Context) {

    private val themeColor = Color.parseColor("#2E7D32") // Agriculture Green
    private val headerTextColor = Color.WHITE
    private val zebraColor = Color.parseColor("#F5F5F5") // Light Gray
    private val borderColor = Color.parseColor("#E0E0E0")

    fun generatePdf(
        customer: Customer,
        transactions: List<CustomerTransaction>,
        openingBalance: Double,
        startDate: Long?,
        endDate: Long?
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = themeColor
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = headerTextColor
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var yPosition = 50f
        val margin = 40f
        val pageWidth = pageInfo.pageWidth.toFloat()
        val contentWidth = pageWidth - (margin * 2)

        // Title
        canvas.drawText("كشف حساب عميل تفصيلي", pageWidth / 2f, yPosition, titlePaint)
        yPosition += 40f

        // Info Section
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("العميل: ${customer.name}", pageWidth - margin, yPosition, paint.apply { textAlign = Paint.Align.RIGHT })
        yPosition += 20f

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        if (startDate != null && endDate != null) {
            canvas.drawText("الفترة: ${sdf.format(Date(startDate))} - ${sdf.format(Date(endDate))}", pageWidth - margin, yPosition, paint)
            yPosition += 30f
        } else {
            canvas.drawText("كشف حساب شامل", pageWidth - margin, yPosition, paint)
            yPosition += 30f
        }

        // Table Setup
        val colWidths = floatArrayOf(70f, 130f, 50f, 50f, 70f, 70f, 75f) // Total 515. Page is 595.
        val headers = arrayOf("التاريخ", "نوع العملية", "الكمية", "السعر", "مدين", "دائن", "التراكمي")
        
        // Draw Header Background
        val headerHeight = 25f
        paint.color = themeColor
        canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + headerHeight - 15f, paint)

        // Draw Header Text
        var currentX = margin
        for (i in headers.indices) {
            canvas.drawText(headers[i], currentX + colWidths[i] - 5f, yPosition + 3f, headerPaint)
            currentX += colWidths[i]
        }
        yPosition += headerHeight

        fun formatDecimal(value: Double?): String {
            if (value == null) return "-"
            return if (value == value.toLong().toDouble()) {
                String.format(Locale.ENGLISH, "%.0f", value)
            } else {
                String.format(Locale.ENGLISH, "%,.2f", value)
            }
        }

        // Row Drawing Helper
        fun drawRow(vals: Array<String>, isZebra: Boolean, currentY: Float) {
            if (isZebra) {
                paint.color = zebraColor
                canvas.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 5f, paint)
            }
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.RIGHT
            var x = margin
            for (i in vals.indices) {
                canvas.drawText(vals[i], x + colWidths[i] - 5f, currentY, paint)
                x += colWidths[i]
            }
        }

        // Opening Balance
        drawRow(arrayOf(
            "-", "رصيد افتتاحي", "-", "-", "-", "-", formatDecimal(openingBalance)
        ), false, yPosition)
        yPosition += 20f

        // Transactions
        transactions.forEachIndexed { index, trans ->
            if (yPosition > pageInfo.pageHeight - 80f) {
                // Draw Footer before finishing page
                canvas.drawText("صفحة ${pdfDocument.pages.size + 1}", pageWidth / 2f, pageInfo.pageHeight - 20f, footerPaint)
                
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
                
                // Redraw Headers on new page
                paint.color = themeColor
                canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + headerHeight - 15f, paint)
                var nx = margin
                for (i in headers.indices) {
                    canvas.drawText(headers[i], nx + colWidths[i] - 5f, yPosition + 3f, headerPaint)
                    nx += colWidths[i]
                }
                yPosition += headerHeight
            }

            drawRow(arrayOf(
                sdf.format(Date(trans.date)),
                trans.typeName,
                formatDecimal(trans.netWeight),
                formatDecimal(trans.price),
                if (trans.debit > 0) formatDecimal(trans.debit) else "-",
                if (trans.credit > 0) formatDecimal(trans.credit) else "-",
                formatDecimal(trans.cumulativeBalance)
            ), index % 2 == 0, yPosition)
            yPosition += 20f
        }

        // Final Footer
        canvas.drawText("صفحة ${pdfDocument.pages.size + 1} | صدر بتاريخ ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", pageWidth / 2f, pageInfo.pageHeight - 20f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "Customer_Statement_${customer.name}_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }
}
