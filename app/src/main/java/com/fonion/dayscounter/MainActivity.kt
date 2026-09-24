package com.fonion.dayscounter

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.fonion.dayscounter.databinding.ActivityMainBinding
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "days_counter_prefs"
        private const val KEY_START_EPOCH = "start_epoch_millis"
        private const val KEY_TIMEZONE = "timezone_id"
        private const val MAX_DAYS = 5445L // 15 years

        // A friendly curated list. "Device default" resolves to the phone's own zone.
        private val TIMEZONE_CHOICES = linkedMapOf(
            "Device default" to null,
            "GMT / UTC" to "GMT",
            "London (Europe/London)" to "Europe/London",
            "New York (America/New_York)" to "America/New_York",
            "Los Angeles (America/Los_Angeles)" to "America/Los_Angeles",
            "Chicago (America/Chicago)" to "America/Chicago",
            "Sao Paulo (America/Sao_Paulo)" to "America/Sao_Paulo",
            "Berlin/Paris (Europe/Paris)" to "Europe/Paris",
            "Moscow (Europe/Moscow)" to "Europe/Moscow",
            "Dubai (Asia/Dubai)" to "Asia/Dubai",
            "Mumbai (Asia/Kolkata)" to "Asia/Kolkata",
            "Singapore (Asia/Singapore)" to "Asia/Singapore",
            "Tokyo (Asia/Tokyo)" to "Asia/Tokyo",
            "Sydney (Australia/Sydney)" to "Australia/Sydney",
            "Auckland (Pacific/Auckland)" to "Pacific/Auckland"
        )
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        ensureStartDate()
        refreshUi()

        binding.btnTimezone.setOnClickListener { showTimezoneDialog() }
        binding.btnReset.setOnClickListener { showResetConfirmation() }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun ensureStartDate() {
        if (!prefs.contains(KEY_START_EPOCH)) {
            prefs.edit().putLong(KEY_START_EPOCH, Instant.now().toEpochMilli()).apply()
        }
    }

    private fun currentZoneId(): ZoneId {
        val stored = prefs.getString(KEY_TIMEZONE, null)
        return if (stored.isNullOrEmpty()) ZoneId.systemDefault() else ZoneId.of(stored)
    }

    private fun currentZoneLabel(): String {
        val stored = prefs.getString(KEY_TIMEZONE, null)
        return TIMEZONE_CHOICES.entries.firstOrNull { it.value == stored }?.key
            ?: stored
            ?: "Device default"
    }

    private fun refreshUi() {
        val startEpoch = prefs.getLong(KEY_START_EPOCH, Instant.now().toEpochMilli())
        val zone = currentZoneId()

        val startInstant = Instant.ofEpochMilli(startEpoch)
        val nowInstant = Instant.now()

        val startZdt = ZonedDateTime.ofInstant(startInstant, zone)
        val nowZdt = ZonedDateTime.ofInstant(nowInstant, zone)

        val daysPassed = ChronoUnit.DAYS.between(startInstant, nowInstant).coerceAtLeast(0)
        val cappedDays = daysPassed.coerceAtMost(MAX_DAYS)
        val daysRemaining = (MAX_DAYS - daysPassed).coerceAtLeast(0)
        val percent = (daysPassed.toDouble() / MAX_DAYS.toDouble() * 100.0).coerceIn(0.0, 100.0)

        binding.tvDaysPassed.text = cappedDays.toString()
        binding.tvDaysOfTotal.text = getString(R.string.days_of_total, MAX_DAYS.toInt())
        binding.tvPercent.text = getString(R.string.progress_percent, percent)
        binding.progressBar.progress = (percent * 100).toInt().coerceIn(0, 10000)
        binding.tvDaysRemaining.text = getString(R.string.days_remaining, daysRemaining.toInt())
        binding.tvStartDate.text = startZdt.format(dateFormatter)
        binding.tvTimezone.text = currentZoneLabel()

        if (daysPassed >= MAX_DAYS) {
            val finishZdt = startZdt.plusDays(MAX_DAYS)
            binding.tvEndDate.text = finishZdt.format(dateFormatter)
            binding.tvMilestone.visibility = android.view.View.VISIBLE
        } else {
            val finishInstant = startInstant.plus(Duration.ofDays(MAX_DAYS))
            val finishZdt = ZonedDateTime.ofInstant(finishInstant, zone)
            binding.tvEndDate.text = finishZdt.format(dateFormatter)
            binding.tvMilestone.visibility = android.view.View.GONE
        }
    }

    private fun showTimezoneDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_timezone, null)
        val spinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerTimezone)

        val labels = TIMEZONE_CHOICES.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val currentLabel = currentZoneLabel()
        val currentIndex = labels.indexOf(currentLabel).let { if (it >= 0) it else 0 }
        spinner.setSelection(currentIndex)

        AlertDialog.Builder(this)
            .setTitle(R.string.change_timezone)
            .setView(dialogView)
            .setPositiveButton(R.string.apply) { _, _ ->
                val chosenLabel = labels[spinner.selectedItemPosition]
                val chosenZone = TIMEZONE_CHOICES[chosenLabel]
                prefs.edit().putString(KEY_TIMEZONE, chosenZone).apply()
                refreshUi()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reset_confirm_title)
            .setMessage(R.string.reset_confirm_message)
            .setPositiveButton(R.string.confirm_reset) { _, _ ->
                prefs.edit().putLong(KEY_START_EPOCH, Instant.now().toEpochMilli()).apply()
                refreshUi()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
