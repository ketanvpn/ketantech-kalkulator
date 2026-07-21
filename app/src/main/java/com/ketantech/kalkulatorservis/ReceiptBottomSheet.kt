package com.ketantech.kalkulatorservis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ketantech.kalkulatorservis.databinding.BottomSheetReceiptBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bottom Sheet untuk menampilkan nota sebagai popup modal.
 * Bisa di-swipe down untuk tutup, atau klik tombol Selesai.
 */
class ReceiptBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetReceiptBinding? = null
    private val binding get() = _binding!!

    private val rupiah: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    private val displayDateFormat =
        SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))

    // Data yang akan ditampilkan
    var receiptNumber: String = ""
    var deviceName: String = ""
    var customerName: String = ""
    var result: CalculationResult? = null
    var serviceLevel: ServiceLevel = ServiceLevel.LEVEL_1
    var warrantyDays: Int = 7
    var estimatedHours: Int = 0
    var onShareClick: (() -> Unit)? = null
    var onSaveImageClick: (() -> Unit)? = null
    var onDoneClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderData()
        setupButtons()
    }

    private fun renderData() {
        val res = result ?: return

        binding.tvReceiptNumber.text = receiptNumber
        binding.tvReceiptDate.text = displayDateFormat.format(Date())

        binding.tvReceiptDevice.text =
            if (deviceName.isEmpty()) getString(R.string.receipt_no_device) else deviceName

        binding.tvReceiptCustomer.text = getString(
            R.string.receipt_customer_format,
            customerName.ifEmpty { getString(R.string.receipt_default_customer) }
        )

        // Tampilkan/sembunyikan baris sparepart
        val visibility = if (res.showSparepartRows) View.VISIBLE else View.GONE
        binding.rowSparepart.visibility = visibility
        binding.rowRiskFund.visibility = visibility

        binding.tvSparepartValue.text = rupiah.format(res.sparepartCost)
        binding.tvRiskFundValue.text = rupiah.format(res.riskFund)
        binding.tvOperationalValue.text = rupiah.format(res.operationalCost)

        binding.tvServiceFeeLabel.text =
            getString(R.string.receipt_service_fee_level, serviceLevel.tier)
        binding.tvServiceFeeValue.text = rupiah.format(res.serviceFee)

        binding.tvTotalValue.text = rupiah.format(res.total)

        binding.tvWarrantyMessage.text =
            getString(R.string.receipt_warranty_message, warrantyDays)

        // Estimasi waktu (P1)
        if (estimatedHours > 0) {
            binding.tvEstimatedTime.visibility = View.VISIBLE
            binding.tvEstimatedTime.text = getString(R.string.receipt_estimated_time) +
                ": " + getString(R.string.receipt_estimated_format, estimatedHours)
        } else {
            binding.tvEstimatedTime.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        binding.btnShare.setOnClickListener {
            onShareClick?.invoke()
        }
        binding.btnSaveImage.setOnClickListener {
            onSaveImageClick?.invoke()
        }
        binding.btnDone.setOnClickListener {
            onDoneClick?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReceiptBottomSheet"

        fun newInstance(
            receiptNumber: String,
            deviceName: String,
            customerName: String,
            result: CalculationResult,
            serviceLevel: ServiceLevel,
            warrantyDays: Int,
            estimatedHours: Int = 0,
            onShareClick: () -> Unit,
            onSaveImageClick: () -> Unit = {},
            onDoneClick: () -> Unit
        ): ReceiptBottomSheet {
            return ReceiptBottomSheet().apply {
                this.receiptNumber = receiptNumber
                this.deviceName = deviceName
                this.customerName = customerName
                this.result = result
                this.serviceLevel = serviceLevel
                this.warrantyDays = warrantyDays
                this.estimatedHours = estimatedHours
                this.onShareClick = onShareClick
                this.onSaveImageClick = onSaveImageClick
                this.onDoneClick = onDoneClick
            }
        }
    }
}
