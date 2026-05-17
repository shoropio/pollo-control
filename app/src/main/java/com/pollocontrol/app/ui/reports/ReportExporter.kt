package com.pollocontrol.app.ui.reports

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.pollocontrol.app.domain.model.BatchReport
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object ReportExporter {

    fun exportToCsv(reports: List<BatchReport>): String {
        val sb = StringBuilder()
        sb.appendLine("Lote,Raza,Fecha Ingreso,Edad (dias),Cant. Inicial,Pollos Vivos,Mortandad,Mortalidad %,Alimento Total (kg),Conversion,Peso Promedio (kg),Costo Total,Costo/Pollo,Costo/Kilo,Ventas Totales,Utilidad,Rentabilidad %")
        for (r in reports) {
            sb.appendLine(
                "${r.batchName},${r.breed},${r.entryDate},${r.age}," +
                        "${r.initialCount},${r.aliveCount},${r.totalMortality}," +
                        "${"%.1f".format(r.mortalityPercentage)},${r.totalFeed}," +
                        "${"%.2f".format(r.feedConversion)},${"%.3f".format(r.avgWeight)}," +
                        "${"%.2f".format(r.totalCost)},${"%.2f".format(r.costPerChicken)}," +
                        "${"%.2f".format(r.costPerKilo)},${"%.2f".format(r.totalSales)}," +
                        "${"%.2f".format(r.profit)},${"%.1f".format(r.profitability)}"
            )
        }
        return sb.toString()
    }

    fun exportToPdf(reports: List<BatchReport>): ByteArray {
        val document = PdfDocument()
        val pageWidth = 792
        val pageHeight = 1128
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply { textSize = 26f; isFakeBoldText = true }
        val subtitlePaint = Paint().apply { textSize = 14f }
        val headerPaint = Paint().apply { textSize = 15f; isFakeBoldText = true }
        val dataPaint = Paint().apply { textSize = 12f }
        val labelPaint = Paint().apply { textSize = 11f; color = android.graphics.Color.GRAY }
        val dividerPaint = Paint().apply { strokeWidth = 1f; color = android.graphics.Color.LTGRAY }

        var y = 50f

        canvas.drawText("Reportes - PolloControl", 40f, y, titlePaint)
        y += 36f
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date())
        canvas.drawText("Generado: $dateStr", 40f, y, subtitlePaint)
        y += 32f

        for (r in reports) {
            if (y > pageHeight - 120f) break

            canvas.drawLine(40f, y, pageWidth - 40f, y, dividerPaint)
            y += 12f
            canvas.drawText(r.batchName, 40f, y, headerPaint)
            y += 24f

            fun drawField(label: String, value: String, x: Float) {
                canvas.drawText(label, x, y, labelPaint)
                canvas.drawText(value, x, y + 16f, dataPaint)
            }

            drawField("Raza:", r.breed, 40f)
            drawField("Cant. Inicial:", "${r.initialCount}", 280f)
            drawField("Pollos Vivos:", "${r.aliveCount}", 520f)
            y += 32f

            drawField("Edad:", "${r.age} dias", 40f)
            drawField("Mortalidad:", "${"%.1f".format(r.mortalityPercentage)}%", 280f)
            drawField("Alimento:", "${"%.2f".format(r.totalFeed)} kg", 520f)
            y += 32f

            drawField("Costo Total:", "$${"%.2f".format(r.totalCost)}", 40f)
            drawField("Ventas:", "$${"%.2f".format(r.totalSales)}", 280f)
            drawField("Utilidad:", "$${"%.2f".format(r.profit)}", 520f)
            y += 32f

            drawField("Conversion:", "${"%.2f".format(r.feedConversion)}", 40f)
            drawField("Peso Prom.:", "${"%.3f".format(r.avgWeight)} kg", 280f)
            drawField("Rentabilidad:", "${"%.1f".format(r.profitability)}%", 520f)

            y += 48f
        }

        document.finishPage(page)
        val baos = ByteArrayOutputStream()
        document.writeTo(baos)
        document.close()
        return baos.toByteArray()
    }
}
