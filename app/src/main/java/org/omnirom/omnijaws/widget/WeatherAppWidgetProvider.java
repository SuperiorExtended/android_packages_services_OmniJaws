/*
 * Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnijaws.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import org.omnirom.omnijaws.Config;
import org.omnirom.omnijaws.R;
import org.omnirom.omnijaws.SettingsActivity;
import org.omnirom.omnijaws.client.OmniJawsClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WeatherAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WeatherAppWidgetProvider";
    private static final boolean LOGGING = false;
    private static final int EXTRA_ERROR_DISABLED = 2;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        if (LOGGING) {
            Log.i(TAG, "onEnabled");
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (LOGGING) {
            Log.i(TAG, "onDisabled");
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int id : appWidgetIds) {
            if (LOGGING) {
                Log.i(TAG, "onDeleted: " + id);
            }
            WeatherAppWidgetConfigure.clearPrefs(context, id);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        int i = 0;
        for (int oldWidgetId : oldWidgetIds) {
            if (LOGGING) {
                Log.i(TAG, "onRestored " + oldWidgetId + " " + newWidgetIds[i]);
            }
            WeatherAppWidgetConfigure.remapPrefs(context, oldWidgetId, newWidgetIds[i]);
            i++;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (LOGGING) {
            Log.i(TAG, "onReceive: " + action);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (LOGGING) {
            Log.i(TAG, "onUpdate");
        }
        updateAllWeather(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        if (LOGGING) {
            Log.i(TAG, "onAppWidgetOptionsChanged");
        }
        updateWeather(context, appWidgetManager, appWidgetId);
    }

    public static void updateAfterConfigure(Context context, int appWidgetId) {
        if (LOGGING) {
            Log.i(TAG, "updateAfterConfigure");
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        updateWeather(context, appWidgetManager, appWidgetId);
    }

    public static void updateAllWeather(Context context) {
        if (LOGGING) {
            Log.i(TAG, "updateAllWeather at = " + new Date());
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            ComponentName componentName = new ComponentName(context, WeatherAppWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            for (int appWidgetId : appWidgetIds) {
                updateWeather(context, appWidgetManager, appWidgetId);
            }
        }
    }

    public static void showErrorState(Context context, int errorReason) {
        if (LOGGING) {
            Log.i(TAG, "showErrorState " + errorReason);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            ComponentName componentName = new ComponentName(context, WeatherAppWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            for (int appWidgetId : appWidgetIds) {
                showError(context, appWidgetManager, appWidgetId, errorReason);
            }
        }
    }

    private static PendingIntent getConfigureIntent(Context context, int appWidgetId) {
        Intent configureIntent = new Intent(context, SettingsActivity.class);
        configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return  PendingIntent.getActivity(context, 0, configureIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void updateWeather(
            Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        if (LOGGING) {
            Log.i(TAG, "updateWeather " + appWidgetId);
        }

        if (!Config.isEnabled(context)) {
            showErrorState(context, EXTRA_ERROR_DISABLED);
            return;
        }

        OmniJawsClient weatherClient = new OmniJawsClient(context);
        weatherClient.queryWeather();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.weather_appwidget);
        initWidget(widget);

        widget.setOnClickPendingIntent(R.id.weather_data, getConfigureIntent(context, appWidgetId));

        boolean backgroundShadow = prefs.getBoolean(WeatherAppWidgetConfigure.KEY_BACKGROUND_SHADOW + "_" + appWidgetId, false);
        widget.setViewVisibility(R.id.background_shadow, backgroundShadow ? View.VISIBLE : View.GONE);

        OmniJawsClient.WeatherInfo weatherData = weatherClient.getWeatherInfo();
        if (weatherData == null) {
            Log.e(TAG, "updateWeather weatherData == null");
            widget.setViewVisibility(R.id.condition_line, View.GONE);
            widget.setViewVisibility(R.id.current_weather_line, View.GONE);
            widget.setViewVisibility(R.id.info_container, View.VISIBLE);
            widget.setTextViewText(R.id.info_text, context.getResources().getString(R.string.omnijaws_service_waiting));
            appWidgetManager.updateAppWidget(appWidgetId, widget);
            return;
        }

        Bundle newOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minHeight = context.getResources().getDimensionPixelSize(R.dimen.weather_widget_height);
        int minWidth = context.getResources().getDimensionPixelSize(R.dimen.weather_widget_width);

        int currentHeight = minHeight;
        int currentWidth = minWidth;

        if (newOptions != null) {
            currentHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight);
            currentWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth);
        }

        boolean withForcast = prefs.getBoolean(WeatherAppWidgetConfigure.KEY_WITH_FORECAST + "_" + appWidgetId, true);
        boolean showDays = (currentHeight > minHeight && withForcast) ? true : false;
        boolean showLocalDetails = (currentHeight > minHeight && withForcast) ? true : false;

        Long timeStamp = weatherData.timeStamp;
        String format = DateFormat.is24HourFormat(context) ? "HH:mm" : "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        widget.setTextViewText(R.id.current_weather_timestamp, sdf.format(timeStamp));

        sdf = new SimpleDateFormat("EE");
        Calendar cal = Calendar.getInstance();
        String dayShort = sdf.format(new Date(cal.getTimeInMillis()));

        Drawable d = weatherClient.getWeatherConditionImage(weatherData.forecasts.get(0).conditionCode);
        BitmapDrawable bd = overlay(context.getResources(), d, weatherData.forecasts.get(0).low, weatherData.forecasts.get(0).high,
                weatherData.tempUnits);
        widget.setImageViewBitmap(R.id.forecast_image_0, bd.getBitmap());
        widget.setTextViewText(R.id.forecast_text_0, dayShort);
        widget.setViewVisibility(R.id.forecast_text_0, showDays ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.forecast_0, withForcast ? View.VISIBLE : View.GONE);

        cal.add(Calendar.DATE, 1);
        dayShort = sdf.format(new Date(cal.getTimeInMillis()));

        d = weatherClient.getWeatherConditionImage(weatherData.forecasts.get(1).conditionCode);
        bd = overlay(context.getResources(), d, weatherData.forecasts.get(1).low, weatherData.forecasts.get(1).high,
                weatherData.tempUnits);
        widget.setImageViewBitmap(R.id.forecast_image_1, bd.getBitmap());
        widget.setTextViewText(R.id.forecast_text_1, dayShort);
        widget.setViewVisibility(R.id.forecast_text_1, showDays ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.forecast_1, withForcast ? View.VISIBLE : View.GONE);

        cal.add(Calendar.DATE, 1);
        dayShort = sdf.format(new Date(cal.getTimeInMillis()));

        d = weatherClient.getWeatherConditionImage(weatherData.forecasts.get(2).conditionCode);
        bd = overlay(context.getResources(), d, weatherData.forecasts.get(2).low, weatherData.forecasts.get(2).high,
                weatherData.tempUnits);
        widget.setImageViewBitmap(R.id.forecast_image_2, bd.getBitmap());
        widget.setTextViewText(R.id.forecast_text_2, dayShort);
        widget.setViewVisibility(R.id.forecast_text_2, showDays ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.forecast_2, withForcast ? View.VISIBLE : View.GONE);

        cal.add(Calendar.DATE, 1);
        dayShort = sdf.format(new Date(cal.getTimeInMillis()));

        d = weatherClient.getWeatherConditionImage(weatherData.forecasts.get(3).conditionCode);
        bd = overlay(context.getResources(), d, weatherData.forecasts.get(3).low, weatherData.forecasts.get(3).high,
                weatherData.tempUnits);
        widget.setImageViewBitmap(R.id.forecast_image_3, bd.getBitmap());
        widget.setTextViewText(R.id.forecast_text_3, dayShort);
        widget.setViewVisibility(R.id.forecast_text_3, showDays ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.forecast_3, withForcast ? View.VISIBLE : View.GONE);

        cal.add(Calendar.DATE, 1);
        dayShort = sdf.format(new Date(cal.getTimeInMillis()));

        d = weatherClient.getWeatherConditionImage(weatherData.forecasts.get(4).conditionCode);
        bd = overlay(context.getResources(), d, weatherData.forecasts.get(4).low, weatherData.forecasts.get(4).high,
                weatherData.tempUnits);
        widget.setImageViewBitmap(R.id.forecast_image_4, bd.getBitmap());
        widget.setTextViewText(R.id.forecast_text_4, dayShort);
        widget.setViewVisibility(R.id.forecast_text_4, showDays ? View.VISIBLE : View.GONE);
        widget.setViewVisibility(R.id.forecast_4, withForcast ? View.VISIBLE : View.GONE);

        d = weatherClient.getWeatherConditionImage(weatherData.conditionCode);
        bd = overlay(context.getResources(), d, weatherData.temp, null, weatherData.tempUnits);
        widget.setImageViewBitmap(R.id.current_image, bd.getBitmap());
        widget.setTextViewText(R.id.current_text, context.getResources().getText(R.string.omnijaws_current_text));
        widget.setViewVisibility(R.id.current_text, showDays ? View.VISIBLE : View.GONE);

        widget.setViewVisibility(R.id.current_weather_line, showLocalDetails ? View.VISIBLE : View.GONE);
        widget.setTextViewText(R.id.current_weather_city, weatherData.city);
        widget.setTextViewText(R.id.current_weather_data, weatherData.windSpeed + " " + weatherData.windUnits + " "
                + weatherData.pinWheel + " - " + weatherData.humidity);

        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    private static void showError(
            Context context, AppWidgetManager appWidgetManager, int appWidgetId, int errorReason) {

        if (LOGGING) {
            Log.i(TAG, "showError " + appWidgetId + " errorReason = " + errorReason);
        }

        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.weather_appwidget);
        initWidget(widget);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundShadow = prefs.getBoolean(WeatherAppWidgetConfigure.KEY_BACKGROUND_SHADOW + "_" + appWidgetId, false);
        widget.setViewVisibility(R.id.background_shadow, backgroundShadow ? View.VISIBLE : View.GONE);

        widget.setOnClickPendingIntent(R.id.weather_data, getConfigureIntent(context, appWidgetId));

        if (errorReason == EXTRA_ERROR_DISABLED) {
            widget.setViewVisibility(R.id.condition_line, View.GONE);
            widget.setViewVisibility(R.id.current_weather_line, View.GONE);
            widget.setViewVisibility(R.id.info_container, View.VISIBLE);
            widget.setTextViewText(R.id.info_text, context.getResources().getString(R.string.omnijaws_service_disabled));
        } else {
            // should never happen
            widget.setViewVisibility(R.id.condition_line, View.GONE);
            widget.setViewVisibility(R.id.current_weather_line, View.GONE);
            widget.setViewVisibility(R.id.info_container, View.VISIBLE);
            widget.setTextViewText(R.id.info_text, context.getResources().getString(R.string.omnijaws_service_unkown));
        }

        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    private static void initWidget(RemoteViews widget) {
        widget.setViewVisibility(R.id.condition_line, View.VISIBLE);
        widget.setViewVisibility(R.id.current_weather_line, View.VISIBLE);
        widget.setViewVisibility(R.id.info_container, View.GONE);
    }

    private static BitmapDrawable overlay(Resources resources, Drawable image, String min, String max, String tempUnits) {
        if (image instanceof VectorDrawable) {
            image = applyTint(image);
        }
        final Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        final float density = resources.getDisplayMetrics().density;
        final int footerHeight = Math.round(18 * density);
        final int imageWidth = image.getIntrinsicWidth();
        final int imageHeight = image.getIntrinsicHeight();
        final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
        textPaint.setTypeface(font);
        textPaint.setColor(resources.getColor(R.color.widget_text_color));
        textPaint.setTextAlign(Paint.Align.LEFT);
        final int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.getDisplayMetrics());
        textPaint.setTextSize(textSize);
        final int height = imageHeight + footerHeight;
        final int width = imageWidth;

        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bmp);
        image.setBounds(0, 0, imageWidth, imageHeight);
        image.draw(canvas);

        String str = null;
        if (max != null) {
            str = min + "/" + max + tempUnits;
        } else {
            str = min + tempUnits;
        }
        Rect bounds = new Rect();
        textPaint.getTextBounds(str, 0, str.length(), bounds);
        canvas.drawText(str, width / 2 - bounds.width() / 2, height - textSize / 2, textPaint);

        return shadow(resources, bmp);
    }

    private static Drawable applyTint(Drawable icon) {
        icon = icon.mutate();
        icon.setTint(Color.WHITE);
        return icon;
    }

    public static BitmapDrawable shadow(Resources resources, Drawable image) {
        final Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        final int imageWidth = image.getIntrinsicWidth();
        final int imageHeight = image.getIntrinsicHeight();
        final Bitmap b = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);
        image.setBounds(0, 0, imageWidth, imageHeight);
        image.draw(canvas);

        BlurMaskFilter blurFilter = new BlurMaskFilter(5,
                BlurMaskFilter.Blur.OUTER);
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setMaskFilter(blurFilter);

        int[] offsetXY = new int[2];
        Bitmap b2 = b.extractAlpha(shadowPaint, offsetXY);

        Bitmap bmResult = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
                Bitmap.Config.ARGB_8888);

        canvas.setBitmap(bmResult);
        canvas.drawBitmap(b2, offsetXY[0], offsetXY[1], null);
        canvas.drawBitmap(b, 0, 0, null);

        return new BitmapDrawable(resources, bmResult);
    }

    public static BitmapDrawable shadow(Resources resources, Bitmap b) {
        final Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
                Paint.FILTER_BITMAP_FLAG));

        BlurMaskFilter blurFilter = new BlurMaskFilter(5, BlurMaskFilter.Blur.OUTER);
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setMaskFilter(blurFilter);

        int[] offsetXY = new int[2];
        Bitmap b2 = b.extractAlpha(shadowPaint, offsetXY);

        Bitmap bmResult = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
                Bitmap.Config.ARGB_8888);

        canvas.setBitmap(bmResult);
        canvas.drawBitmap(b2, offsetXY[0], offsetXY[1], null);
        canvas.drawBitmap(b, 0, 0, null);

        return new BitmapDrawable(resources, bmResult);
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context, WeatherAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        for (int appWidgetId : appWidgetIds) {
            WeatherAppWidgetProvider.updateWeather(context, appWidgetManager, appWidgetId);
        }
    }

    public static void disableAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context, WeatherAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        for (int appWidgetId : appWidgetIds) {
            WeatherAppWidgetProvider.showError(context, appWidgetManager, appWidgetId, EXTRA_ERROR_DISABLED);
        }
    }
}
