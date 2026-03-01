package apps.farm.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import apps.farm.data.model.Cycle
import apps.farm.data.model.Farm
import apps.farm.data.model.SaleInvoice
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CycleReportPdfGenerator(private val context: Context) {

    private val themeColor = Color.parseColor("#2E7D32") // Agriculture Green
    private val headerTextColor = Color.WHITE
    private val zebraColor = Color.parseColor("#F5F5F5") // Light Gray
    private val summaryBgColor = Color.parseColor("#E8F5E9") // Very Light Green

    data class CycleReportItem(
        val type: String,
        val merchantName: String,
        val weight: Double,
        val price: Double,
        val credit: Double, // الدائن
        val debit: Double // المدين
    )

    fun generatePdf(
        farm: Farm,
        cycle: Cycle,
        items: List<CycleReportItem>
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

        // Title
        canvas.drawText("كشف حساب مزرعة \"${farm.farmName}\" دورة \"${cycle.cycleName}\"", pageWidth / 2f, yPosition, titlePaint)
        yPosition += 40f

        // Info Section
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("المزرعة: ${farm.farmName}", pageWidth - margin, yPosition, paint.apply { textAlign = Paint.Align.RIGHT })
        yPosition += 20f
        canvas.drawText("الدورة: ${cycle.cycleName}", pageWidth - margin, yPosition, paint)
        yPosition += 30f

        // Table Setup
        val headers = arrayOf("البيان / التاجر", "الوزن", "السعر", "المدين", "الدائن")
        val colWidths = floatArrayOf(215f, 60f, 60f, 90f, 90f) // Total 515
        
        // Draw Header Background
        val headerHeight = 25f
        paint.color = themeColor
        canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + headerHeight - 15f, paint)

        // Draw Headers
        var currentX = pageWidth - margin
        for (i in headers.indices) {
            canvas.drawText(headers[i], currentX - 5f, yPosition + 3f, headerPaint)
            currentX -= colWidths[i]
        }
        yPosition += headerHeight

        // Row Drawing Helper
        fun drawRow(vals: Array<String>, isZebra: Boolean, currentY: Float, isSummary: Boolean = false) {
            if (isSummary) {
                paint.color = summaryBgColor
                canvas.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 5f, paint)
                paint.isFakeBoldText = true
            } else if (isZebra) {
                paint.color = zebraColor
                canvas.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 5f, paint)
                paint.isFakeBoldText = false
            } else {
                paint.isFakeBoldText = false
            }
            
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.RIGHT
            var x = pageWidth - margin
            for (i in vals.indices) {
                canvas.drawText(vals[i], x - 5f, currentY, paint)
                x -= colWidths[i]
            }
            paint.isFakeBoldText = false
        }

        var totalCredit = 0.0
        var totalDebit = 0.0

        fun formatNum(value: Double): String {
            if (value == 0.0) return ""
            return if (value == value.toLong().toDouble()) {
                String.format(Locale.ENGLISH, "%,.0f", value)
            } else {
                String.format(Locale.ENGLISH, "%,.2f", value)
            }
        }

        items.forEachIndexed { index, item ->
            if (yPosition > pageInfo.pageHeight - 120f) {
                canvas.drawText("صفحة ${pdfDocument.pages.size + 1}", pageWidth / 2f, pageInfo.pageHeight - 20f, footerPaint)
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
                
                // Re-draw headers
                paint.color = themeColor
                canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + headerHeight - 15f, paint)
                var nx = pageWidth - margin
                for (i in headers.indices) {
                    canvas.drawText(headers[i], nx - 5f, yPosition + 3f, headerPaint)
                    nx -= colWidths[i]
                }
                yPosition += headerHeight
            }

            drawRow(arrayOf(
                "${item.type} - ${item.merchantName}",
                formatNum(item.weight),
                formatNum(item.price),
                formatNum(item.debit),
                formatNum(item.credit)
            ), index % 2 == 1, yPosition)
            
            totalCredit += item.credit
            totalDebit += item.debit
            yPosition += 20f
        }

        // Summary Highlight
        yPosition += 10f
        drawRow(arrayOf(
            "الإجمالي", "", "",
            String.format(Locale.ENGLISH, "%,.2f", totalDebit),
            String.format(Locale.ENGLISH, "%,.2f", totalCredit)
        ), false, yPosition, isSummary = true)
        
        yPosition += 40f
        val net = totalDebit - totalCredit
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        paint.color = themeColor
        canvas.drawText("الصافي النهائي: ${String.format(Locale.ENGLISH, "%,.2f", net)}", pageWidth - margin, yPosition, paint)

        // Footer
        canvas.drawText("صفحة ${pdfDocument.pages.size + 1} | تقرير دورة مفصل | ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", pageWidth / 2f, pageInfo.pageHeight - 20f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "Cycle_Report_${farm.farmName}_${cycle.cycleName}_${System.currentTimeMillis()}.pdf")
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
