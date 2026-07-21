package com.ketantech.kalkulatorservis

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.ketantech.kalkulatorservis.databinding.ActivityOnboardingBinding

/**
 * Onboarding 3-slide untuk pengguna pertama.
 * Slide 1: Selamat datang | Slide 2: Cara pakai | Slide 3: Transparansi
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: OnboardingPrefs

    private val slides = listOf(
        OnboardingSlide(
            emoji = "🔧",
            title = "Selamat Datang",
            description = "Kalkulator servis HP yang membuat harga transparan dan konsisten untuk pelanggan Anda."
        ),
        OnboardingSlide(
            emoji = "📝",
            title = "Isi & Lihat Hasil",
            description = "Masukkan data perangkat dan level jasa. Nota rincian biaya akan muncul otomatis di bawah."
        ),
        OnboardingSlide(
            emoji = "🤝",
            title = "Bangun Kepercayaan",
            description = "Bagikan nota ke WhatsApp pelanggan. Harga jujur = pelanggan loyal."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = OnboardingPrefs(this)

        binding.viewPager.adapter = OnboardingAdapter(slides)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                binding.btnNext.text = if (position == slides.size - 1) "Mulai" else "Lanjut"
            }
        })

        setupIndicators()
        updateIndicators(0)

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < slides.size - 1) {
                binding.viewPager.currentItem++
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener { finishOnboarding() }
    }

    private fun setupIndicators() {
        binding.layoutIndicators.removeAllViews()
        repeat(slides.size) {
            val dot = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    marginEnd = 8
                }
                setImageResource(R.drawable.indicator_dot_inactive)
            }
            binding.layoutIndicators.addView(dot)
        }
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until binding.layoutIndicators.childCount) {
            val dot = binding.layoutIndicators.getChildAt(i) as ImageView
            dot.setImageResource(
                if (i == position) R.drawable.indicator_dot_active
                else R.drawable.indicator_dot_inactive
            )
        }
    }

    private fun finishOnboarding() {
        prefs.hasSeenOnboarding = true
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    data class OnboardingSlide(val emoji: String, val title: String, val description: String)

    inner class OnboardingAdapter(private val slides: List<OnboardingSlide>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder>() {

        inner class SlideViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val emoji: TextView = view.findViewById(R.id.tvEmoji)
            val title: TextView = view.findViewById(R.id.tvTitle)
            val description: TextView = view.findViewById(R.id.tvDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
            val view = layoutInflater.inflate(R.layout.item_onboarding_slide, parent, false)
            return SlideViewHolder(view)
        }

        override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
            val slide = slides[position]
            holder.emoji.text = slide.emoji
            holder.title.text = slide.title
            holder.description.text = slide.description
        }

        override fun getItemCount() = slides.size
    }
}
