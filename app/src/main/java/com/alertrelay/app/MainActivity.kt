package com.alertrelay.app

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.alertrelay.app.data.ThemeMode
import com.alertrelay.app.databinding.ActivityMainBinding
import com.alertrelay.app.databinding.ItemSetupStepBinding
import com.alertrelay.app.ntfy.AlertSender
import com.alertrelay.app.ntfy.AlertType
import com.alertrelay.app.service.RelayForegroundService
import com.alertrelay.app.util.PermissionHelper
import com.alertrelay.app.util.ThemeHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val prefs by lazy { (application as AlertRelayApp).prefs }
    private var pulseAnimator: ObjectAnimator? = null
    private var lastStatusKind: StatusKind? = null

    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshUi() }

    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshUi() }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshUi() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        applySystemBars()
        initSetupStepLabels()

        binding.editTopic.setText(prefs.ntfyTopic)
        binding.switchRelay.isChecked = prefs.relayEnabled
        binding.switchIncludeDetails.isChecked = prefs.includeDetails
        updateThemeButtonIcon()

        binding.btnSaveTopic.setOnClickListener { saveTopic() }
        binding.switchRelay.setOnCheckedChangeListener { _, checked ->
            onRelayToggled(checked)
        }
        binding.switchIncludeDetails.setOnCheckedChangeListener { _, checked ->
            prefs.includeDetails = checked
            RelayForegroundService.sync(this)
            animateCard(binding.cardPrivacy)
            refreshUi()
        }
        binding.btnTheme.setOnClickListener { cycleTheme() }
        binding.btnPhonePermission.setOnClickListener {
            phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
        binding.btnNotificationAccess.setOnClickListener {
            PermissionHelper.openNotificationListenerSettings(this)
        }
        binding.btnContactsPermission.setOnClickListener {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
        binding.btnBattery.setOnClickListener {
            PermissionHelper.openBatteryOptimizationSettings(this)
        }
        binding.btnTestCall.setOnClickListener { runTest(AlertType.TEST_CALL) }
        binding.btnTestSms.setOnClickListener { runTest(AlertType.TEST_SMS) }

        if (PermissionHelper.needsPostNotificationsPermission() &&
            !PermissionHelper.hasPostNotificationsPermission(this)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        RelayForegroundService.sync(this)
        binding.contentContainer.scheduleLayoutAnimation()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        RelayForegroundService.sync(this)
        refreshUi()
    }

    override fun onDestroy() {
        pulseAnimator?.cancel()
        super.onDestroy()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootCoordinator) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBar.updatePadding(top = bars.top)
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun applySystemBars() {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    private fun initSetupStepLabels() {
        setStepLabel(binding.stepPhone, R.string.setup_step_phone)
        setStepLabel(binding.stepNotification, R.string.setup_step_notifications)
        setStepLabel(binding.stepTopic, R.string.setup_step_topic)
        setStepLabel(binding.stepContacts, R.string.setup_step_contacts)
    }

    private fun setStepLabel(step: ItemSetupStepBinding, labelRes: Int) {
        step.stepLabel.setText(labelRes)
    }

    private fun saveTopic() {
        val topic = binding.editTopic.text?.toString()?.trim().orEmpty()
        if (topic.length < 4) {
            binding.topicInputLayout.error = getString(R.string.toast_topic_required)
            binding.topicInputLayout.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.scale_pop)
            )
            return
        }
        binding.topicInputLayout.error = null
        prefs.ntfyTopic = topic
        animateCard(binding.cardTopic)
        Snackbar.make(binding.root, R.string.toast_topic_saved, Snackbar.LENGTH_SHORT).show()
        refreshUi()
    }

    private fun onRelayToggled(enabled: Boolean) {
        if (enabled && prefs.ntfyTopic.isBlank()) {
            binding.switchRelay.isChecked = false
            Snackbar.make(binding.root, R.string.toast_topic_required, Snackbar.LENGTH_LONG).show()
            return
        }
        if (enabled && !PermissionHelper.hasPhonePermission(this)) {
            binding.switchRelay.isChecked = false
            Snackbar.make(binding.root, R.string.setup_step_phone, Snackbar.LENGTH_LONG)
                .setAction(R.string.grant_phone) {
                    phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                }
                .show()
            return
        }

        prefs.relayEnabled = enabled
        RelayForegroundService.sync(this)
        animateCard(binding.cardStatus)
        Snackbar.make(
            binding.root,
            if (enabled) R.string.toast_relay_enabled else R.string.toast_relay_disabled,
            Snackbar.LENGTH_SHORT
        ).show()
        refreshUi()
    }

    private fun cycleTheme() {
        val next = when (prefs.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        prefs.themeMode = next
        ThemeHelper.apply(next)
        updateThemeButtonIcon()
        binding.btnTheme.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_pop))
        val message = when (next) {
            ThemeMode.SYSTEM -> R.string.theme_system
            ThemeMode.LIGHT -> R.string.theme_light
            ThemeMode.DARK -> R.string.theme_dark
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun updateThemeButtonIcon() {
        val iconRes = when (prefs.themeMode) {
            ThemeMode.SYSTEM -> R.drawable.ic_theme_system
            ThemeMode.LIGHT -> R.drawable.ic_theme_light
            ThemeMode.DARK -> R.drawable.ic_theme_dark
        }
        binding.btnTheme.setIconResource(iconRes)
    }

    private fun runTest(type: AlertType) {
        val topic = binding.editTopic.text?.toString()?.trim().orEmpty()
        if (topic.isBlank()) {
            Snackbar.make(binding.root, R.string.toast_topic_required, Snackbar.LENGTH_LONG).show()
            return
        }
        prefs.ntfyTopic = topic

        binding.btnTestCall.isEnabled = false
        binding.btnTestSms.isEnabled = false

        val sampleDetail = if (prefs.includeDetails) {
            getString(R.string.test_contact_sample)
        } else {
            null
        }

        lifecycleScope.launch {
            val ok = AlertSender.sendAndWait(this@MainActivity, type, sampleDetail)
            binding.btnTestCall.isEnabled = true
            binding.btnTestSms.isEnabled = true
            val msg = if (ok) R.string.toast_test_sent else R.string.toast_test_failed
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            if (ok) animateCard(binding.cardTest)
        }
    }

    private fun refreshUi() {
        val hasTopic = prefs.ntfyTopic.isNotBlank()
        val hasPhone = PermissionHelper.hasPhonePermission(this)
        val hasListener = PermissionHelper.hasNotificationListenerAccess(this)
        val hasContacts = PermissionHelper.hasContactsPermission(this)
        val relayOn = prefs.relayEnabled && hasTopic && hasPhone

        binding.switchRelay.isChecked = prefs.relayEnabled
        binding.switchIncludeDetails.isChecked = prefs.includeDetails

        updateStep(binding.stepPhone, hasPhone)
        updateStep(binding.stepNotification, hasListener)
        updateStep(binding.stepTopic, hasTopic)
        updateStep(binding.stepContacts, hasContacts)

        val completed = listOf(hasPhone, hasListener, hasTopic, hasContacts).count { it }
        binding.setupProgress.max = 4
        animateProgress(binding.setupProgress.progress, completed)
        binding.setupProgressLabel.text = getString(R.string.setup_progress, completed, 4)

        binding.btnPhonePermission.visibility = if (hasPhone) View.GONE else View.VISIBLE
        binding.btnNotificationAccess.visibility = if (hasListener) View.GONE else View.VISIBLE
        binding.btnContactsPermission.visibility = if (hasContacts) View.GONE else View.VISIBLE

        binding.privacyFooter.text = if (prefs.includeDetails) {
            getString(R.string.privacy_note)
        } else {
            getString(R.string.privacy_note_generic)
        }

        val kind = when {
            relayOn && hasListener -> StatusKind.ACTIVE
            relayOn -> StatusKind.WARNING
            prefs.relayEnabled -> StatusKind.SETUP
            else -> StatusKind.PAUSED
        }
        applyStatus(kind)
    }

    private fun applyStatus(kind: StatusKind) {
        if (lastStatusKind != kind) {
            binding.cardStatus.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_pop))
            lastStatusKind = kind
        }

        val card = binding.cardStatus
        val icon = binding.statusIcon
        val pulse = binding.statusPulse

        when (kind) {
            StatusKind.ACTIVE -> {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_active_bg))
                card.strokeColor = ContextCompat.getColor(this, R.color.status_active_stroke)
                card.strokeWidth = (resources.displayMetrics.density).toInt().coerceAtLeast(1)
                icon.setImageResource(R.drawable.ic_status_active)
                binding.statusTitle.text = getString(R.string.status_active)
                binding.statusSubtitle.text = if (prefs.includeDetails) {
                    getString(R.string.include_details_subtitle)
                } else {
                    getString(R.string.privacy_note_generic)
                }
                pulse.visibility = View.VISIBLE
                startPulse(pulse)
            }
            StatusKind.WARNING -> {
                stopPulse()
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_warning_bg))
                card.strokeWidth = 0
                icon.setImageResource(R.drawable.ic_status_warning)
                binding.statusTitle.text = getString(R.string.status_active)
                binding.statusSubtitle.text = getString(R.string.setup_step_notifications)
                pulse.visibility = View.GONE
            }
            StatusKind.SETUP -> {
                stopPulse()
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_warning_bg))
                card.strokeWidth = 0
                icon.setImageResource(R.drawable.ic_status_warning)
                binding.statusTitle.text = getString(R.string.status_missing_setup)
                binding.statusSubtitle.text = getString(R.string.section_connection_hint)
                pulse.visibility = View.GONE
            }
            StatusKind.PAUSED -> {
                stopPulse()
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_inactive_bg))
                card.strokeWidth = 0
                icon.setImageResource(R.drawable.ic_status_paused)
                binding.statusTitle.text = getString(R.string.status_inactive)
                binding.statusSubtitle.text = if (prefs.includeDetails) {
                    getString(R.string.privacy_note)
                } else {
                    getString(R.string.privacy_note_generic)
                }
                pulse.visibility = View.GONE
            }
        }
    }

    private fun updateStep(step: ItemSetupStepBinding, done: Boolean) {
        val wasDone = step.stepCheck.tag == true
        step.stepCheck.setImageResource(
            if (done) R.drawable.ic_check_done else R.drawable.ic_check_pending
        )
        step.stepCheck.tag = done
        step.stepLabel.setTextColor(
            ContextCompat.getColor(
                this,
                if (done) R.color.on_surface else R.color.on_surface_variant
            )
        )
        if (done && !wasDone) {
            step.stepCheck.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_pop))
        }
    }

    private fun animateProgress(from: Int, to: Int) {
        if (from == to) {
            binding.setupProgress.progress = to
            return
        }
        ValueAnimator.ofInt(from, to).apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            addUpdateListener { binding.setupProgress.progress = it.animatedValue as Int }
            start()
        }
    }

    private fun animateCard(card: View) {
        card.animate()
            .scaleX(0.98f)
            .scaleY(0.98f)
            .setDuration(90)
            .withEndAction {
                card.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(160)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun startPulse(view: View) {
        if (pulseAnimator?.isRunning == true) return
        pulseAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.35f, 1f).apply {
            duration = 1100
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun stopPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        binding.statusPulse.alpha = 1f
    }

    private enum class StatusKind {
        ACTIVE,
        WARNING,
        SETUP,
        PAUSED,
    }
}
