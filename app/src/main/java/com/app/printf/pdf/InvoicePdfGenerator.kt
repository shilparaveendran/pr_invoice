package com.app.printf.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import com.app.printf.R
import com.app.printf.data.entity.CompanyProfile
import com.app.printf.data.model.InvoiceWithItems
import com.app.printf.util.Formatters
import com.app.printf.util.InvoiceNumberUtils
import com.app.printf.util.TaxCalculator
import com.app.printf.util.TaxConstants
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object InvoicePdfGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private const val OUTER_LEFT = 36f
    private const val OUTER_TOP = 42f
    private const val OUTER_RIGHT = PAGE_WIDTH - 36f
    private const val OUTER_BOTTOM = PAGE_HEIGHT - 42f

    private const val HEADER_LINE_SPACING = 12f
    private const val HEADER_NAME_ADDRESS_GAP = 10f
    private const val HEADER_INFO_TEXT_SIZE = 9.5f
    private const val HEADER_ICON_SIZE = 10f
    private const val PDF_BASE_TEXT_SIZE = 10f
    private const val PDF_COMPANY_NAME_SIZE = 28f
    private const val PDF_TAX_INVOICE_SIZE = 11.5f
    private const val HEADER_ICON_GAP = 5f
    private const val HEADER_INFO_MAX_TEXT_WIDTH = 460f
    private const val META_H = 66f
    private const val PARTY_H = 118f
    private const val PARTY_ADDRESS_LINE_SPACING = 13f
    private const val PARTY_SALES_TYPE_TEXT_SIZE = 9f
    private const val PARTY_SALES_TYPE_GAP = 10f
    private const val TABLE_HEADER_H = 28f
    private const val TABLE_ROW_H = 24f
    private const val TABLE_LINE_HEIGHT = 11f
    private const val TABLE_CELL_MAX_LINES = 2
    private const val TABLE_CELL_PADDING = 4f
    private const val MIN_TABLE_BODY_ROWS = 10
    private const val FOOTER_MIN_H = 220f
    private const val SUMMARY_ROW_H = 18f
    private const val SIGNATURE_LABEL_OFFSET = 10f
    private const val SIGNATURE_LABEL_GAP = 6f
    private const val SIGNATURE_MAX_HEIGHT = 58f
    private const val SIGNATURE_FOR_GAP = 8f
    private const val SIGNATURE_MAX_WIDTH_RATIO = 0.95f

    private const val COLOR_BLACK = Color.BLACK
    private val COLOR_TAX_INVOICE = Color.rgb(192, 96, 0)

    fun generate(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        companyProfile: CompanyProfile,
    ): File {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.lineItems

        val companyName = companyProfile.companyName.ifBlank { "PR ENGINEERING" }
        val companyAddress = companyProfile.address.ifBlank { "Puttekkad, Feroke" }

        val subTotal = items.sumOf { it.unitPrice * it.quantity }
        val salesType = invoice.salesType
            .ifBlank { companyProfile.salesType }
            .ifBlank { TaxConstants.STATE_SALE }
        val isInterstate = TaxCalculator.isInterstateSale(salesType)
        val taxTotals = TaxCalculator.invoiceTotals(subTotal, isInterstate)
        val sgst = taxTotals.sgst
        val cgst = taxTotals.cgst
        val igst = taxTotals.igst
        val totalGst = taxTotals.totalGst
        val gross = taxTotals.gross
        val roundedGrand = taxTotals.grandTotal
        val rounding = taxTotals.rounding

        val dir = File(context.filesDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${InvoiceNumberUtils.sanitizeForFileName(invoice.invoiceNumber)}.pdf")

        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create())
        val canvas = page.canvas

        val stroke = Paint().apply {
            style = Paint.Style.STROKE
            color = COLOR_BLACK
            strokeWidth = 1f
            isAntiAlias = true
        }
        val text = Paint().apply {
            style = Paint.Style.FILL
            color = COLOR_BLACK
            textSize = PDF_BASE_TEXT_SIZE
            isAntiAlias = true
        }
        val bold = Paint(text).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val right = Paint(text).apply { textAlign = Paint.Align.RIGHT }
        val rightBold = Paint(bold).apply { textAlign = Paint.Align.RIGHT }
        val center = Paint(text).apply { textAlign = Paint.Align.CENTER }
        val thanksMessagePaint = Paint(bold).apply {
            textAlign = Paint.Align.CENTER
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            textSkewX = -0.2f
            isFakeBoldText = true
        }

        canvas.drawColor(Color.WHITE)
        canvas.drawRect(OUTER_LEFT, OUTER_TOP, OUTER_RIGHT, OUTER_BOTTOM, stroke)

        var y = OUTER_TOP

        // 1) Header — company name + icon info block (address, GSTIN, email, phone)
        val centerX = (OUTER_LEFT + OUTER_RIGHT) / 2f
        val headerInfoPaint = Paint(text).apply { textSize = HEADER_INFO_TEXT_SIZE }
        val headerRows = buildHeaderInfoRows(companyProfile, companyAddress, headerInfoPaint)
        val contentHeaderHeight = 34f + HEADER_NAME_ADDRESS_GAP + (headerRows.size * HEADER_LINE_SPACING) + 10f
        val headerHeight = max(contentHeaderHeight, 78f)
        val headerBottom = y + headerHeight

        canvas.drawRect(OUTER_LEFT, y, OUTER_RIGHT, headerBottom, stroke)

        loadPdfLogoBitmap(context)?.let { logo ->
            drawHeaderLogo(canvas, logo, OUTER_LEFT, y, headerHeight)
        }

        val companyNamePaint = Paint(bold).apply {
            textSize = PDF_COMPANY_NAME_SIZE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(companyName.uppercase(), centerX, y + 30f, companyNamePaint)

        drawHeaderInfoBlock(
            canvas = canvas,
            centerX = centerX,
            startBaselineY = y + 38f + HEADER_NAME_ADDRESS_GAP,
            rows = headerRows,
            textPaint = headerInfoPaint,
        )

        val taxInvoicePaint = Paint(rightBold).apply { color = COLOR_TAX_INVOICE }
        taxInvoicePaint.textSize = PDF_TAX_INVOICE_SIZE
        canvas.drawText("TAX INVOICE", OUTER_RIGHT - 8f, y + 16f, taxInvoicePaint)

        y = headerBottom

        // 2) Invoice meta block
        val metaBottom = y + META_H
        val metaMidX = OUTER_LEFT + (OUTER_RIGHT - OUTER_LEFT) * 0.57f
        canvas.drawRect(OUTER_LEFT, y, OUTER_RIGHT, metaBottom, stroke)
        canvas.drawLine(metaMidX, y, metaMidX, metaBottom, stroke)

        val metaRowH = META_H / 4f
        for (i in 1..3) {
            val lineY = y + (i * metaRowH)
            canvas.drawLine(OUTER_LEFT, lineY, OUTER_RIGHT, lineY, stroke)
        }

        val invNo = InvoiceNumberUtils.formatForPdf(invoice.invoiceNumber)
        val dateText = Formatters.formatDate(invoice.dateMillis)
        val dueDate = Formatters.formatDate(invoice.dateMillis + (15L * 24 * 60 * 60 * 1000))

        drawMetaRow(canvas, y + metaRowH * 0 + 12f, OUTER_LEFT + 6f, "Invoice Number:", invNo, bold, text)
        drawMetaRow(canvas, y + metaRowH * 1 + 12f, OUTER_LEFT + 6f, "Invoice date:", dateText, bold, text)
        drawMetaRow(canvas, y + metaRowH * 2 + 12f, OUTER_LEFT + 6f, "Terms", "15", bold, text)
        drawMetaRow(canvas, y + metaRowH * 3 + 12f, OUTER_LEFT + 6f, "Due Date", dueDate, bold, text)

        drawMetaRow(canvas, y + metaRowH * 0 + 12f, metaMidX + 6f, "Place Of Supply", companyProfile.state.ifBlank { "Kerala(32)" }, bold, text)
        val ewayBill = invoice.ewayBillNo.ifBlank { "-" }
        val buyerPo = invoice.buyerPoNo.ifBlank { "-" }
        drawMetaRow(canvas, y + metaRowH * 1 + 12f, metaMidX + 6f, "E-Way Bill", ewayBill, bold, text)
        drawMetaRow(canvas, y + metaRowH * 2 + 12f, metaMidX + 6f, "Buyer PO No.", buyerPo, bold, text)

        y = metaBottom

        // 3) Bill/Ship block
        val partyBottom = y + PARTY_H
        val partyMidX = (OUTER_LEFT + OUTER_RIGHT) / 2f
        canvas.drawRect(OUTER_LEFT, y, OUTER_RIGHT, partyBottom, stroke)
        canvas.drawLine(partyMidX, y, partyMidX, partyBottom, stroke)

        bold.textSize = PDF_BASE_TEXT_SIZE
        canvas.drawText("Bill To", OUTER_LEFT + 6f, y + 13f, bold)
        canvas.drawText("Ship To", partyMidX + 6f, y + 13f, bold)

        val partyHeaderLineY = y + 18f
        canvas.drawLine(OUTER_LEFT, partyHeaderLineY, OUTER_RIGHT, partyHeaderLineY, stroke)

        val billName = invoice.billToName.ifBlank { invoice.customerName }
        val shipName = invoice.shipToName.ifBlank { billName }
        val billAddress = invoice.billToAddress.ifBlank { invoice.customerAddress }
        val shipAddress = invoice.shipToAddress.ifBlank { billAddress }
        val billGstin = invoice.billToGstin.ifBlank { "-" }
        val shipGstin = invoice.shipToGstin.ifBlank { "-" }
        val billMobile = invoice.billToMobile.trim()
        val shipMobile = invoice.shipToMobile.trim()

        bold.textSize = 12f
        canvas.drawText(billName.uppercase(), OUTER_LEFT + 6f, y + 36f, bold)
        canvas.drawText(shipName.uppercase(), partyMidX + 6f, y + 36f, bold)

        text.textSize = 9.5f
        val partyColumnWidth = partyMidX - OUTER_LEFT - 12f
        val billLines = splitPartyAddressLines(billAddress, text, partyColumnWidth)
        val shipLines = splitPartyAddressLines(shipAddress, text, partyColumnWidth)
        var billY = y + 48f
        billLines.forEach { line ->
            canvas.drawText(line, OUTER_LEFT + 6f, billY, text)
            billY += PARTY_ADDRESS_LINE_SPACING
        }
        if (billMobile.isNotBlank()) {
            canvas.drawText("Mob: $billMobile", OUTER_LEFT + 6f, billY, text)
            billY += PARTY_ADDRESS_LINE_SPACING
        }
        canvas.drawText("GSTIN: $billGstin", OUTER_LEFT + 6f, billY, text)

        var shipY = y + 48f
        shipLines.forEach { line ->
            canvas.drawText(line, partyMidX + 6f, shipY, text)
            shipY += PARTY_ADDRESS_LINE_SPACING
        }
        if (shipMobile.isNotBlank()) {
            canvas.drawText("Mob: $shipMobile", partyMidX + 6f, shipY, text)
            shipY += PARTY_ADDRESS_LINE_SPACING
        }
        canvas.drawText("GSTIN: $shipGstin", partyMidX + 6f, shipY, text)

        val salesRowTop = partyBottom - 18f
        canvas.drawLine(OUTER_LEFT, salesRowTop, OUTER_RIGHT, salesRowTop, stroke)
        val salesTypeLabel = "Sales Type:"
        val salesTypeBaseline = partyBottom - 4f
        val salesTypeLabelPaint = Paint(bold).apply { textSize = PARTY_SALES_TYPE_TEXT_SIZE }
        val salesTypeValuePaint = Paint(text).apply { textSize = PARTY_SALES_TYPE_TEXT_SIZE }
        val salesTypeLabelX = OUTER_LEFT + 6f
        canvas.drawText(salesTypeLabel, salesTypeLabelX, salesTypeBaseline, salesTypeLabelPaint)
        val salesTypeValueX = salesTypeLabelX + salesTypeLabelPaint.measureText(salesTypeLabel) + PARTY_SALES_TYPE_GAP
        canvas.drawText(salesType, salesTypeValueX, salesTypeBaseline, salesTypeValuePaint)

        y = partyBottom

        // 4) Items table
        val tableTop = y
        val colPerc = floatArrayOf(0.07f, 0.28f, 0.09f, 0.09f, 0.12f, 0.10f, 0.25f)
        val colX = FloatArray(colPerc.size + 1)
        colX[0] = OUTER_LEFT
        for (i in colPerc.indices) {
            colX[i + 1] = colX[i] + ((OUTER_RIGHT - OUTER_LEFT) * colPerc[i])
        }

        val headers = listOf(
            "SL NO",
            "Description of Goods",
            "HSN/SAC",
            "Quantity",
            "Unit Price",
            "Tax Rate",
            "Gross Amount",
        )
        bold.textSize = 9f
        val tableHeaderH = TABLE_HEADER_H

        canvas.drawRect(OUTER_LEFT, tableTop, OUTER_RIGHT, tableTop + tableHeaderH, stroke)
        colX.forEach { xLine -> canvas.drawLine(xLine, tableTop, xLine, tableTop + tableHeaderH, stroke) }

        headers.forEachIndexed { index, header ->
            val maxWidth = columnWidth(colX, index)
            val headerPaint = scaledPaintForText(header, bold, maxWidth)
            drawTableTextLines(
                canvas = canvas,
                lines = listOf(header),
                columnLeft = colX[index],
                columnRight = colX[index + 1],
                firstBaselineY = tableTop + 18f,
                lineHeight = TABLE_LINE_HEIGHT,
                paint = headerPaint,
                align = Paint.Align.CENTER,
                truncateWithEllipsis = false,
            )
        }

        var rowY = tableTop + tableHeaderH
        text.textSize = 9.5f
        right.textSize = 9.5f
        center.textSize = 9.5f
        val lineTaxRate = "18%"

        for (i in items.indices) {
            val item = items[i]
            val lineAmount = item.unitPrice * item.quantity
            val rowTop = rowY

            val descriptionLines = wrapTextToMaxLines(
                text = item.productName.uppercase(),
                paint = bold,
                maxWidth = columnWidth(colX, 1),
                maxLines = TABLE_CELL_MAX_LINES,
            )
            val hsnLines = wrapTextToMaxLines(
                text = item.hsn,
                paint = text,
                maxWidth = columnWidth(colX, 2),
                maxLines = TABLE_CELL_MAX_LINES,
            )
            val qtyLines = wrapTextToMaxLines(
                text = item.quantity.toString(),
                paint = text,
                maxWidth = columnWidth(colX, 3),
                maxLines = TABLE_CELL_MAX_LINES,
            )
            val rowLineCount = maxOf(descriptionLines.size, hsnLines.size, qtyLines.size, 1)
            val rowHeight = tableRowHeight(rowLineCount)
            val firstBaselineY = rowTop + 14f

            drawTableTextLines(
                canvas = canvas,
                lines = listOf((i + 1).toString()),
                columnLeft = colX[0],
                columnRight = colX[1],
                firstBaselineY = firstBaselineY,
                lineHeight = TABLE_LINE_HEIGHT,
                paint = text,
                align = Paint.Align.CENTER,
            )
            drawTableTextLines(
                canvas = canvas,
                lines = descriptionLines,
                columnLeft = colX[1],
                columnRight = colX[2],
                firstBaselineY = firstBaselineY,
                lineHeight = TABLE_LINE_HEIGHT,
                paint = bold,
            )
            drawTableTextLines(
                canvas = canvas,
                lines = hsnLines,
                columnLeft = colX[2],
                columnRight = colX[3],
                firstBaselineY = firstBaselineY,
                lineHeight = TABLE_LINE_HEIGHT,
                paint = text,
            )
            drawTableTextLines(
                canvas = canvas,
                lines = qtyLines,
                columnLeft = colX[3],
                columnRight = colX[4],
                firstBaselineY = firstBaselineY,
                lineHeight = TABLE_LINE_HEIGHT,
                paint = text,
            )
            drawColumnAmount(
                canvas,
                colX[4],
                colX[5],
                firstBaselineY,
                Formatters.formatPdfAmount(item.unitPrice),
                right,
            )
            drawTableTextLines(
                canvas = canvas,
                lines = listOf(lineTaxRate),
                columnLeft = colX[5],
                columnRight = colX[6],
                firstBaselineY = firstBaselineY,
                lineHeight = TABLE_LINE_HEIGHT,
                paint = center,
                align = Paint.Align.CENTER,
            )
            drawColumnAmount(
                canvas,
                colX[6],
                colX[7],
                firstBaselineY,
                Formatters.formatPdfAmount(lineAmount),
                right,
            )
            rowY += rowHeight
        }

        val minTableBottom = tableTop + tableHeaderH + (MIN_TABLE_BODY_ROWS * TABLE_ROW_H)
        val itemsBottom = rowY
        val bottomTop = max(itemsBottom, minTableBottom).coerceAtMost(OUTER_BOTTOM - FOOTER_MIN_H)

        // 5) Bottom section split
        val splitX = OUTER_LEFT + (OUTER_RIGHT - OUTER_LEFT) * 0.64f
        canvas.drawLine(OUTER_LEFT, bottomTop, OUTER_RIGHT, bottomTop, stroke)
        canvas.drawRect(OUTER_LEFT, bottomTop, OUTER_RIGHT, OUTER_BOTTOM, stroke)
        canvas.drawLine(splitX, bottomTop, splitX, OUTER_BOTTOM, stroke)

        // Complete vertical table borders only for used rows.
        colX.forEach { xLine -> canvas.drawLine(xLine, tableTop, xLine, bottomTop, stroke) }

        // Left: thanks + bank details
        canvas.drawText("Thanks For Your Business.", (OUTER_LEFT + splitX) / 2f, bottomTop + 32f, thanksMessagePaint)

        bold.textSize = PDF_BASE_TEXT_SIZE
        text.textSize = PDF_BASE_TEXT_SIZE
        canvas.drawText("Our Bank Details", OUTER_LEFT + 8f, bottomTop + 62f, bold)
        drawAlignedLabelValueRows(
            canvas = canvas,
            left = OUTER_LEFT + 8f,
            right = splitX - 8f,
            firstBaselineY = bottomTop + 80f,
            lineHeight = 14f,
            rows = listOf(
                "Account Holder Name" to companyProfile.accountHolderName.ifBlank { companyName.uppercase() },
                "Account Number" to companyProfile.accountNumber.ifBlank { "N/A" },
                "IFSC" to companyProfile.ifscCode.ifBlank { "N/A" },
                "Account Type" to companyProfile.accountType.ifBlank { "CURRENT" },
                "Bank" to companyProfile.bankName.ifBlank { "N/A" },
            ),
            labelPaint = text,
            valuePaint = bold,
        )

        // Right: tax summary
        val signatureLayout = signatureBlockLayout()
        var sy = bottomTop
        drawSummary(canvas, splitX, OUTER_RIGHT, sy, "Total Amount", subTotal, stroke, text, right)
        sy += SUMMARY_ROW_H
        if (isInterstate) {
            drawSummary(canvas, splitX, OUTER_RIGHT, sy, "IGST @18%", igst, stroke, text, right)
            sy += SUMMARY_ROW_H
        } else {
            drawSummary(canvas, splitX, OUTER_RIGHT, sy, "SGST @9%", sgst, stroke, text, right)
            sy += SUMMARY_ROW_H
            drawSummary(canvas, splitX, OUTER_RIGHT, sy, "CGST @9%", cgst, stroke, text, right)
            sy += SUMMARY_ROW_H
        }
        drawSummary(canvas, splitX, OUTER_RIGHT, sy, "Total GST Amt", totalGst, stroke, text, right)
        sy += SUMMARY_ROW_H
        drawSummary(canvas, splitX, OUTER_RIGHT, sy, "Sub Total", gross, stroke, bold, rightBold)
        sy += SUMMARY_ROW_H
        drawSummary(canvas, splitX, OUTER_RIGHT, sy, "Rounding off", rounding, stroke, text, right)
        sy += SUMMARY_ROW_H
        drawSummary(canvas, splitX, OUTER_RIGHT, sy, "Grand Total", roundedGrand, stroke, bold, rightBold)

        text.textSize = PDF_BASE_TEXT_SIZE
        canvas.drawText("For ${companyName.uppercase()}", splitX + 8f, signatureLayout.forLineBaseline, text)

        drawAuthorizedSignatureBlock(
            canvas = canvas,
            signaturePath = companyProfile.signaturePath,
            cellLeft = splitX,
            cellRight = OUTER_RIGHT,
            layout = signatureLayout,
            center = center,
        )

        document.finishPage(page)
        FileOutputStream(file).use { output -> document.writeTo(output) }
        document.close()
        return file
    }

    private data class SignatureBlockLayout(
        val forLineBaseline: Float,
        val signatureAreaTop: Float,
        val signatureAreaBottom: Float,
        val labelBaseline: Float,
        val maxSignatureHeight: Float,
    )

    private fun signatureBlockLayout(): SignatureBlockLayout {
        val labelBaseline = OUTER_BOTTOM - SIGNATURE_LABEL_OFFSET
        val signatureAreaBottom = labelBaseline - SIGNATURE_LABEL_GAP
        val signatureAreaTop = signatureAreaBottom - SIGNATURE_MAX_HEIGHT
        val forLineBaseline = signatureAreaTop - SIGNATURE_FOR_GAP
        return SignatureBlockLayout(
            forLineBaseline = forLineBaseline,
            signatureAreaTop = signatureAreaTop,
            signatureAreaBottom = signatureAreaBottom,
            labelBaseline = labelBaseline,
            maxSignatureHeight = SIGNATURE_MAX_HEIGHT,
        )
    }

    private fun drawAuthorizedSignatureBlock(
        canvas: Canvas,
        signaturePath: String,
        cellLeft: Float,
        cellRight: Float,
        layout: SignatureBlockLayout,
        center: Paint,
    ) {
        val label = "Authorized Signature"
        val horizontalPadding = 8f
        val innerLeft = cellLeft + horizontalPadding
        val innerRight = cellRight - horizontalPadding
        val cellCenterX = (innerLeft + innerRight) / 2f
        val cellWidth = innerRight - innerLeft

        center.textSize = 9.5f
        canvas.drawText(label, cellCenterX, layout.labelBaseline, center)

        loadSignatureBitmap(signaturePath)?.let { signature ->
            val maxWidth = cellWidth * SIGNATURE_MAX_WIDTH_RATIO
            val scale = min(maxWidth / signature.width, layout.maxSignatureHeight / signature.height)
            val width = signature.width * scale
            val height = signature.height * scale
            val destLeft = cellCenterX - (width / 2f)
            val destBottom = layout.signatureAreaBottom
            val destTop = destBottom - height
            val dest = RectF(destLeft, destTop, destLeft + width, destBottom)
            canvas.drawBitmap(signature, null, dest, null)
        }
    }

    private fun loadSignatureBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        return BitmapFactory.decodeFile(path)
    }

    private fun loadPdfLogoBitmap(context: Context): Bitmap? {
        val logoId = context.resources.getIdentifier("pr_invoice_logo", "drawable", context.packageName)
        if (logoId != 0) {
            BitmapFactory.decodeResource(context.resources, logoId)?.let { return it }
        }
        return drawableToBitmap(context, R.drawable.splash_logo_inset)
            ?: drawableToBitmap(context, R.drawable.ic_launcher_foreground)
    }

    private fun drawableToBitmap(context: Context, drawableResId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return null
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 256
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 256
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(bitmapCanvas)
        return bitmap
    }

    private fun drawHeaderLogo(
        canvas: Canvas,
        bitmap: Bitmap,
        left: Float,
        top: Float,
        headerHeight: Float,
    ) {
        val maxWidth = 100f
        val maxHeight = headerHeight - 14f
        val scale = min(maxWidth / bitmap.width, maxHeight / bitmap.height)
        val width = bitmap.width * scale
        val height = bitmap.height * scale
        val destLeft = left + 10f
        val destTop = top + (headerHeight - height) / 2f
        val dest = RectF(destLeft, destTop, destLeft + width, destTop + height)
        canvas.drawBitmap(bitmap, null, dest, null)
    }

    private fun drawColumnAmount(
        canvas: Canvas,
        columnLeft: Float,
        columnRight: Float,
        y: Float,
        value: String,
        paint: Paint,
    ) {
        val padding = 5f
        val fitted = fitTextToWidth(value, paint, columnRight - columnLeft - (padding * 2f))
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(fitted, columnRight - padding, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun fitTextToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var trimmed = text
        while (trimmed.length > 1 && paint.measureText("$trimmed…") > maxWidth) {
            trimmed = trimmed.dropLast(1)
        }
        return if (trimmed.length < text.length) "$trimmed…" else trimmed
    }

    private fun drawAlignedLabelValueRows(
        canvas: Canvas,
        left: Float,
        right: Float,
        firstBaselineY: Float,
        lineHeight: Float,
        rows: List<Pair<String, String>>,
        labelPaint: Paint,
        valuePaint: Paint,
    ) {
        if (rows.isEmpty()) return

        val colon = ":"
        val colonGap = 6f
        val valueGap = 10f
        val maxLabelWidth = rows.maxOf { labelPaint.measureText(it.first) }
        val labelRight = left + maxLabelWidth
        val colonX = labelRight + colonGap
        val valueX = colonX + labelPaint.measureText(colon) + valueGap
        val valueMaxWidth = (right - valueX).coerceAtLeast(0f)

        val alignedLabelPaint = Paint(labelPaint).apply { textAlign = Paint.Align.RIGHT }
        val colonPaint = Paint(labelPaint).apply { textAlign = Paint.Align.LEFT }
        val alignedValuePaint = Paint(valuePaint).apply { textAlign = Paint.Align.LEFT }

        rows.forEachIndexed { index, (label, value) ->
            val y = firstBaselineY + (index * lineHeight)
            canvas.drawText(label, labelRight, y, alignedLabelPaint)
            canvas.drawText(colon, colonX, y, colonPaint)
            canvas.drawText(fitTextToWidth(value, alignedValuePaint, valueMaxWidth), valueX, y, alignedValuePaint)
        }
    }

    private fun drawMetaRow(
        canvas: Canvas,
        y: Float,
        x: Float,
        label: String,
        value: String,
        valuePaint: Paint,
        labelPaint: Paint,
    ) {
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x + 92f, y, valuePaint)
    }

    private fun drawSummary(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        label: String,
        value: Double,
        stroke: Paint,
        labelPaint: Paint,
        valuePaint: Paint,
    ) {
        val bottom = top + SUMMARY_ROW_H
        val valueColLeft = left + (right - left) * 0.46f
        canvas.drawRect(left, top, right, bottom, stroke)
        canvas.drawLine(valueColLeft, top, valueColLeft, bottom, stroke)

        val textBaseline = top + (SUMMARY_ROW_H * 0.72f)
        val labelFitted = fitTextToWidth(label, labelPaint, valueColLeft - left - 8f)
        canvas.drawText(labelFitted, left + 4f, textBaseline, labelPaint)

        val summaryValuePaint = Paint(valuePaint).apply { textSize = 9f }
        drawColumnAmount(
            canvas,
            valueColLeft,
            right,
            textBaseline,
            Formatters.formatPdfAmount(value),
            summaryValuePaint,
        )
    }

    private enum class HeaderInfoIcon {
        LOCATION,
        GST,
        EMAIL,
        PHONE,
        NONE,
    }

    private data class HeaderInfoRow(
        val text: String,
        val icon: HeaderInfoIcon,
        val isAddressLine: Boolean = false,
    )

    private fun buildHeaderInfoRows(
        companyProfile: CompanyProfile,
        address: String,
        paint: Paint,
    ): List<HeaderInfoRow> {
        val rows = mutableListOf<HeaderInfoRow>()
        val addressLine = formatHeaderAddressOneLine(
            address = address,
            paint = paint,
            maxWidth = HEADER_INFO_MAX_TEXT_WIDTH,
        )
        rows.add(
            HeaderInfoRow(
                text = addressLine,
                icon = HeaderInfoIcon.LOCATION,
                isAddressLine = true,
            ),
        )

        val gstin = companyProfile.gstin.trim()
        if (gstin.isNotBlank()) {
            rows.add(HeaderInfoRow(text = "GSTIN:$gstin", icon = HeaderInfoIcon.GST))
        }
        if (companyProfile.email.isNotBlank()) {
            rows.add(HeaderInfoRow(text = "Email:${companyProfile.email.trim()}", icon = HeaderInfoIcon.EMAIL))
        }
        if (companyProfile.phone.isNotBlank()) {
            rows.add(HeaderInfoRow(text = companyProfile.phone.trim(), icon = HeaderInfoIcon.PHONE))
        }
        return rows
    }

    private fun drawHeaderInfoBlock(
        canvas: Canvas,
        centerX: Float,
        startBaselineY: Float,
        rows: List<HeaderInfoRow>,
        textPaint: Paint,
    ) {
        if (rows.isEmpty()) return

        val iconPaint = Paint(textPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.9f
        }

        var baselineY = startBaselineY
        rows.forEach { row ->
            if (row.isAddressLine) {
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(row.text, centerX, baselineY, textPaint)
                if (row.icon == HeaderInfoIcon.LOCATION) {
                    val textWidth = textPaint.measureText(row.text)
                    val iconLeft = centerX - (textWidth / 2f) - HEADER_ICON_GAP - HEADER_ICON_SIZE
                    drawHeaderInfoIcon(
                        canvas = canvas,
                        icon = row.icon,
                        left = iconLeft,
                        baselineY = baselineY,
                        size = HEADER_ICON_SIZE,
                        paint = iconPaint,
                    )
                }
            } else {
                val textWidth = textPaint.measureText(row.text)
                val rowWidth = if (row.icon != HeaderInfoIcon.NONE) {
                    HEADER_ICON_SIZE + HEADER_ICON_GAP + textWidth
                } else {
                    textWidth
                }
                val rowLeft = centerX - (rowWidth / 2f)
                if (row.icon != HeaderInfoIcon.NONE) {
                    drawHeaderInfoIcon(
                        canvas = canvas,
                        icon = row.icon,
                        left = rowLeft,
                        baselineY = baselineY,
                        size = HEADER_ICON_SIZE,
                        paint = iconPaint,
                    )
                }
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(
                    row.text,
                    rowLeft + if (row.icon != HeaderInfoIcon.NONE) {
                        HEADER_ICON_SIZE + HEADER_ICON_GAP
                    } else {
                        0f
                    },
                    baselineY,
                    textPaint,
                )
            }
            baselineY += HEADER_LINE_SPACING
        }
    }

    private fun drawHeaderInfoIcon(
        canvas: Canvas,
        icon: HeaderInfoIcon,
        left: Float,
        baselineY: Float,
        size: Float,
        paint: Paint,
    ) {
        val top = baselineY - size + 1f
        when (icon) {
            HeaderInfoIcon.LOCATION -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(left + size / 2f, top + size * 0.28f, size * 0.18f, paint)
                paint.style = Paint.Style.STROKE
                canvas.drawLine(
                    left + size / 2f,
                    top + size * 0.46f,
                    left + size / 2f,
                    top + size * 0.92f,
                    paint,
                )
                canvas.drawCircle(left + size / 2f, top + size * 0.28f, size * 0.34f, paint)
            }
            HeaderInfoIcon.GST -> {
                paint.style = Paint.Style.STROKE
                canvas.drawCircle(left + size / 2f, top + size / 2f, size * 0.42f, paint)
                val gstPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                    textSize = size * 0.34f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("GST", left + size / 2f, top + size * 0.62f, gstPaint)
            }
            HeaderInfoIcon.EMAIL -> {
                paint.style = Paint.Style.STROKE
                canvas.drawRect(left + 0.5f, top + 1f, left + size - 0.5f, top + size * 0.62f, paint)
                canvas.drawLine(left + 0.5f, top + 1f, left + size / 2f, top + size * 0.34f, paint)
                canvas.drawLine(left + size - 0.5f, top + 1f, left + size / 2f, top + size * 0.34f, paint)
                val atPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                    textSize = size * 0.42f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("@", left + size / 2f, top + size * 0.58f, atPaint)
            }
            HeaderInfoIcon.PHONE -> {
                paint.style = Paint.Style.STROKE
                canvas.drawArc(
                    left + size * 0.12f,
                    top + size * 0.08f,
                    left + size * 0.88f,
                    top + size * 0.92f,
                    135f,
                    160f,
                    false,
                    paint,
                )
                canvas.drawLine(
                    left + size * 0.18f,
                    top + size * 0.82f,
                    left + size * 0.34f,
                    top + size * 0.66f,
                    paint,
                )
                canvas.drawLine(
                    left + size * 0.66f,
                    top + size * 0.34f,
                    left + size * 0.82f,
                    top + size * 0.18f,
                    paint,
                )
            }
            HeaderInfoIcon.NONE -> Unit
        }
    }

    private fun formatHeaderAddressOneLine(
        address: String,
        paint: Paint,
        maxWidth: Float,
    ): String {
        val combined = address
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { "Puttekkad, Feroke" }
        return fitTextToWidth(combined, paint, maxWidth)
    }

    private fun wrapAddressForHeader(address: String, paint: Paint, maxWidth: Float): List<String> {
        return listOf(formatHeaderAddressOneLine(address, paint, maxWidth))
    }

    private fun columnWidth(colX: FloatArray, columnIndex: Int): Float {
        return colX[columnIndex + 1] - colX[columnIndex] - (TABLE_CELL_PADDING * 2f)
    }

    private fun tableRowHeight(lineCount: Int): Float {
        val lines = lineCount.coerceAtLeast(1)
        return max(TABLE_ROW_H, 10f + 14f + ((lines - 1) * TABLE_LINE_HEIGHT))
    }

    private fun scaledPaintForText(text: String, basePaint: Paint, maxWidth: Float): Paint {
        val scaled = Paint(basePaint)
        var size = scaled.textSize
        while (size > 7f && scaled.measureText(text) > maxWidth) {
            size -= 0.25f
            scaled.textSize = size
        }
        return scaled
    }

    private fun drawTableTextLines(
        canvas: Canvas,
        lines: List<String>,
        columnLeft: Float,
        columnRight: Float,
        firstBaselineY: Float,
        lineHeight: Float,
        paint: Paint,
        align: Paint.Align = Paint.Align.LEFT,
        truncateWithEllipsis: Boolean = true,
    ) {
        val maxWidth = columnRight - columnLeft - (TABLE_CELL_PADDING * 2f)
        val x = when (align) {
            Paint.Align.CENTER -> (columnLeft + columnRight) / 2f
            Paint.Align.RIGHT -> columnRight - TABLE_CELL_PADDING
            else -> columnLeft + TABLE_CELL_PADDING
        }
        val linePaint = Paint(paint).apply { textAlign = align }
        lines.forEachIndexed { index, line ->
            val fitted = if (truncateWithEllipsis) {
                fitTextToWidth(line, linePaint, maxWidth)
            } else {
                line
            }
            canvas.drawText(fitted, x, firstBaselineY + (index * lineHeight), linePaint)
        }
    }

    private fun wrapTextToMaxLines(
        text: String,
        paint: Paint,
        maxWidth: Float,
        maxLines: Int,
    ): List<String> {
        val wrapped = wrapTextToWidth(text.trim(), paint, maxWidth)
        if (wrapped.size <= maxLines) return wrapped.ifEmpty { listOf("") }

        val kept = wrapped.take(maxLines - 1).toMutableList()
        val remainder = wrapped.drop(maxLines - 1).joinToString(" ")
        kept.add(fitTextToWidth(remainder, paint, maxWidth))
        return kept
    }

    private fun breakLongWord(word: String, paint: Paint, maxWidth: Float): List<String> {
        if (paint.measureText(word) <= maxWidth) return listOf(word)

        val parts = mutableListOf<String>()
        var chunk = ""
        word.forEach { char ->
            val candidate = chunk + char
            if (paint.measureText(candidate) <= maxWidth) {
                chunk = candidate
            } else {
                if (chunk.isNotEmpty()) parts.add(chunk)
                chunk = char.toString()
            }
        }
        if (chunk.isNotEmpty()) parts.add(chunk)
        return parts.ifEmpty { listOf(word) }
    }

    private fun wrapTextToWidth(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        if (paint.measureText(text) <= maxWidth) return listOf(text)

        val lines = mutableListOf<String>()
        var current = ""
        text.split(Regex("\\s+")).forEach { word ->
            if (word.isBlank()) return@forEach
            val segments = breakLongWord(word, paint, maxWidth)
            segments.forEach { segment ->
                val candidate = if (current.isEmpty()) segment else "$current $segment"
                if (paint.measureText(candidate) <= maxWidth) {
                    current = candidate
                } else {
                    if (current.isNotEmpty()) lines.add(current)
                    current = segment
                }
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines.ifEmpty { listOf(fitTextToWidth(text, paint, maxWidth)) }
    }

    private fun splitPartyAddressLines(address: String, paint: Paint, maxWidth: Float): List<String> {
        val combined = address
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { return emptyList() }

        val wrapped = wrapTextToWidth(combined, paint, maxWidth)
        return if (wrapped.size <= 2) {
            wrapped
        } else {
            wrapTextToMaxLines(combined, paint, maxWidth, maxLines = 3)
        }
    }

    private fun ellipsis(text: String, maxLen: Int): String {
        return if (text.length <= maxLen) text else text.take(max(1, maxLen - 1)) + "..."
    }
}
