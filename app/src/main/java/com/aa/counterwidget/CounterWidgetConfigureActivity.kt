package com.aa.counterwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup

/**
 * The configuration screen for the [NewAppWidget] AppWidget.
 */
class CounterWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var appWidgetBg: Int = -1
    private var onClickListener = View.OnClickListener {
        val context = this@CounterWidgetConfigureActivity

        // When the button is clicked, store the color locally
        val rg = findViewById<View>(R.id.radio_group) as RadioGroup
        val id = rg.checkedRadioButtonId
        val rb = rg.findViewById<View>(id) as RadioButton
        appWidgetBg = (rb.background as ColorDrawable).color

        savePref(context, appWidgetId, COLOR, appWidgetBg)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId, 0, appWidgetBg)
        Log.d(WIDGET, "in configure onclick")

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.counter_widget_configure)
        Log.d(WIDGET, "in configure create")
        findViewById<View>(R.id.add_button).setOnClickListener(onClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

}


