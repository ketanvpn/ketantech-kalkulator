package com.ketantech.kalkulatorservis

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.ketantech.kalkulatorservis.databinding.BottomSheetModalCalcBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Kalkulator modal sparepart multi-komponen.
 * Hasil total dikembalikan via onApply.
 */
class ModalCalculatorBottomSheet(
    private val onApply: (Long) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetModalCalcBinding? = null
    private val binding get() = _binding!!

    private val rupiah = NumberFormat.getNumberInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    private val componentRows = mutableListOf<View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetModalCalcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Mulai dengan 2 baris kosong
        addComponentRow()
        addComponentRow()
        updateTotal()

        binding.btnAddRow.setOnClickListener { addComponentRow() }
        binding.btnClear.setOnClickListener {
            binding.layoutComponents.removeAllViews()
            componentRows.clear()
            addComponentRow()
            updateTotal()
        }
        binding.btnApply.setOnClickListener {
            onApply(parseTotal())
            dismiss()
        }
    }

    private fun addComponentRow() {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_modal_component, binding.layoutComponents, false)
        binding.layoutComponents.addView(row)
        componentRows.add(row)

        val priceField = row.findViewById<TextInputEditText>(R.id.etComponentPrice)
        priceField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTotal()
            }
        })
    }

    private fun parseTotal(): Long {
        var total = 0L
        for (row in componentRows) {
            val priceField = row.findViewById<TextInputEditText>(R.id.etComponentPrice)
            total += ThousandSeparatorTextWatcher.parse(priceField.text.toString())
        }
        return total
    }

    private fun updateTotal() {
        binding.tvTotal.text = "Rp " + rupiah.format(parseTotal())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
