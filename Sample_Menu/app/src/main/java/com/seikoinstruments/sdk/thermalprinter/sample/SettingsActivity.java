package com.seikoinstruments.sdk.thermalprinter.sample;

import java.util.Locale;

import com.seikoinstruments.sdk.thermalprinter.PrinterManager;
import com.seikoinstruments.sdk.thermalprinter.sample.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

    /** PrinterManager（SDK） */
    private PrinterManager mPrinterManager;

    /** change summary  */
    private SharedPreferences.OnSharedPreferenceChangeListener mListener =
        new SharedPreferences.OnSharedPreferenceChangeListener() {

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if(getString(R.string.key_send_timeout).equals(key)){
                // send timeout
                EditTextPreference txtSendTimeout
                        = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.key_send_timeout));
                if(txtSendTimeout != null) {
                    String strSendTimeout = txtSendTimeout.getText();
                    int intSendTimeout = StringUtil.getInt(strSendTimeout);
                    int validSendTimeout = setSendTimeout(intSendTimeout);
                    if(intSendTimeout != validSendTimeout) {
                        txtSendTimeout.setText(Integer.toString(validSendTimeout));
                        Toast.makeText(getApplicationContext(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
                    } else {
                        txtSendTimeout.setSummary(txtSendTimeout.getText() + " msec");
                    }
                }

            } else if(getString(R.string.key_receive_timeout).equals(key)){
                // receive timeout
                EditTextPreference txtReceiveTimeout
                        = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.key_receive_timeout));
                if(txtReceiveTimeout != null) {
                    String strReceiveTimeout = txtReceiveTimeout.getText();
                    int intReceiveTimeout = StringUtil.getInt(strReceiveTimeout);
                    int validReceiveTimeout = setReceiveTimeout(intReceiveTimeout);
                    if(intReceiveTimeout != validReceiveTimeout) {
                        txtReceiveTimeout.setText(Integer.toString(validReceiveTimeout));
                        Toast.makeText(getApplicationContext(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
                    } else {
                        txtReceiveTimeout.setSummary(txtReceiveTimeout.getText() + " msec");
                    }
                }

            } else if(getString(R.string.key_socket_keeping_time).equals(key)){
                // socket keeping time
                EditTextPreference txtSocketKeepingTime
                        = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.key_socket_keeping_time));
                if(txtSocketKeepingTime != null) {
                    String strSocketKeepingTime = txtSocketKeepingTime.getText();
                    int intSocketKeepingTime = StringUtil.getInt(strSocketKeepingTime);
                    int validSocketKeepingTime = setSocketKeepingTime(intSocketKeepingTime);
                    if(intSocketKeepingTime != validSocketKeepingTime) {
                        txtSocketKeepingTime.setText(Integer.toString(validSocketKeepingTime));
                        Toast.makeText(getApplicationContext(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
                    } else {
                        txtSocketKeepingTime.setSummary(txtSocketKeepingTime.getText() + " msec");
                    }
                }

            } else if(getString(R.string.key_international_character).equals(key)){
                // international character
                ListPreference LstInternationalCharacter
                        = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.key_international_character));
                if(LstInternationalCharacter != null) {
                    String strInternationalCharacter = LstInternationalCharacter.getValue();
                    int intInternationalCharacter = StringUtil.getInt(strInternationalCharacter);
                    int validInternationalCharacter = setInternationalCharacter(intInternationalCharacter);
                    if(intInternationalCharacter != validInternationalCharacter) {
                        LstInternationalCharacter.setValue(Integer.toString(validInternationalCharacter));
                        Toast.makeText(getApplicationContext(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
                    } else {
                        LstInternationalCharacter.setSummary(LstInternationalCharacter.getEntry());
                    }
                }

            } else if(getString(R.string.key_code_page).equals(key)){
                // code page
                ListPreference LstCodePage
                    = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.key_code_page));
                if(LstCodePage != null) {
                    String strCodePage = LstCodePage.getValue();
                    int intCodePage = StringUtil.getInt(strCodePage);
                    int validCodePage = setCodePage(intCodePage);
                    if(intCodePage != validCodePage) {
                        LstCodePage.setValue(Integer.toString(validCodePage));
                        Toast.makeText(getApplicationContext(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
                    } else {
                        LstCodePage.setSummary(LstCodePage.getEntry());
                    }
                }
            }

        }
    };



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        SampleApplication application = (SampleApplication)this.getApplication();
        mPrinterManager = application.getPrinterManager();

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = pref.edit();

        // send timeout
        EditTextPreference txtSendTimeout
                = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.key_send_timeout));
        if(txtSendTimeout != null) {
            String sendTimeout = txtSendTimeout.getText();
            if(StringUtil.isEmpty(sendTimeout)) {
                sendTimeout = "10000";
                editor.putString(getText(R.string.key_send_timeout).toString(), sendTimeout);
            } else {
                 String strSendTimeout = txtSendTimeout.getText();
                 int intSendTimeout = StringUtil.getInt(strSendTimeout);
                 int validSendTimeout = setSendTimeout(intSendTimeout);
                 if(intSendTimeout != validSendTimeout) {
                     sendTimeout = Integer.toString(validSendTimeout);
                     editor.putString(getText(R.string.key_send_timeout).toString(), sendTimeout);
                 }
            }
            txtSendTimeout.setSummary(sendTimeout + " msec");
        }

        // receive timeout
        EditTextPreference txtReceiveTimeout
                = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.key_receive_timeout));
        String receiveTimeout = txtReceiveTimeout.getText();
        if(StringUtil.isEmpty(receiveTimeout)) {
            receiveTimeout = "10000";
            editor.putString(getText(R.string.key_receive_timeout).toString(), receiveTimeout);
        } else {
            String strReceiveTimeout = txtReceiveTimeout.getText();
            int intReceiveTimeout = StringUtil.getInt(strReceiveTimeout);
            int validReceiveTimeout = setReceiveTimeout(intReceiveTimeout);
            if(intReceiveTimeout != validReceiveTimeout) {
                receiveTimeout = Integer.toString(validReceiveTimeout);
                editor.putString(getText(R.string.key_receive_timeout).toString(), receiveTimeout);
            }
        }
        txtReceiveTimeout.setSummary(receiveTimeout + " msec");

        // socket keeping time
        EditTextPreference txtSocketKeepingTime
                = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.key_socket_keeping_time));
        String socketKeepingTime = txtSocketKeepingTime.getText();
        if(StringUtil.isEmpty(socketKeepingTime)) {
            socketKeepingTime = "300000";
            editor.putString(getText(R.string.key_socket_keeping_time).toString(), socketKeepingTime);
        } else {
            String strSocketKeepingTime = txtSocketKeepingTime.getText();
            int intSocketKeepingTime = StringUtil.getInt(strSocketKeepingTime);
            int validSocketKeepingTime = setSocketKeepingTime(intSocketKeepingTime);
            if(intSocketKeepingTime != validSocketKeepingTime) {
                socketKeepingTime = Integer.toString(validSocketKeepingTime);
                editor.putString(getText(R.string.key_socket_keeping_time).toString(), socketKeepingTime);
            }
        }
        txtSocketKeepingTime.setSummary(socketKeepingTime + " msec");

        // international character
        ListPreference LstInternationalCharacter
                = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.key_international_character));
        CharSequence internationalCharacter = LstInternationalCharacter.getEntry();
        if(StringUtil.isEmpty(internationalCharacter)) {
            if(Locale.JAPAN.equals(Locale.getDefault())) {
                internationalCharacter = "JAPAN";
                editor.putString(getText(R.string.key_international_character).toString(), "8");
            } else {
                internationalCharacter = "USA";
                editor.putString(getText(R.string.key_international_character).toString(), "0");
            }

        } else {
            String strInternationalCharacter = LstInternationalCharacter.getValue();
            int intInternationalCharacter = StringUtil.getInt(strInternationalCharacter);
            int validInternationalCharacter = setInternationalCharacter(intInternationalCharacter);
            if(intInternationalCharacter != validInternationalCharacter) {
                internationalCharacter = getInternationalCharacterString(validInternationalCharacter);
                editor.putString(getText(R.string.key_international_character).toString(), Integer.toString(validInternationalCharacter));
                LstInternationalCharacter.setValue(Integer.toString(validInternationalCharacter));
            }
        }
        LstInternationalCharacter.setSummary(internationalCharacter);

        // code page
        ListPreference LstCodePage
            = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.key_code_page));
        CharSequence codePage = LstCodePage.getEntry();
        if(StringUtil.isEmpty(codePage)) {
            if(Locale.JAPAN.equals(Locale.getDefault())) {
                codePage = "Katakana";
                editor.putString(getText(R.string.key_code_page).toString(), "1");
            } else {
                codePage = "Code Page 1252";
                editor.putString(getText(R.string.key_code_page).toString(), "16");
            }

        } else {
            String strCodePage = LstCodePage.getValue();
            int intCodePage = StringUtil.getInt(strCodePage);
            int validCodePage = setCodePage(intCodePage);
            if(intCodePage != validCodePage) {
                codePage = getCodePageString(validCodePage);
                editor.putString(getText(R.string.key_code_page).toString(), Integer.toString(validCodePage));
                LstCodePage.setValue(Integer.toString(validCodePage));
            }
        }
        LstCodePage.setSummary(codePage);

        editor.commit();
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);
    }


    private int setSendTimeout(int sendTimeout) {
        int time;
        if(mPrinterManager != null) {
            mPrinterManager.setSendTimeout(sendTimeout);
            time = mPrinterManager.getSendTimeout();

        } else {
            if(sendTimeout < 100 || sendTimeout > 90000) {
                time = 10000;
            } else {
                time = sendTimeout;
            }
        }

        return time;
    }


    private int setReceiveTimeout(int receiveTimeout) {
        int time;
        if(mPrinterManager != null) {
            mPrinterManager.setReceiveTimeout(receiveTimeout);
            time = mPrinterManager.getReceiveTimeout();

        } else {
            if(receiveTimeout < 100 || receiveTimeout > 90000) {
                time = 10000;
            } else {
                time = receiveTimeout;
            }
        }

        return time;
    }


    private int setSocketKeepingTime(int socketKeepingTime) {
        int time;
        if(mPrinterManager != null) {
            mPrinterManager.setSocketKeepingTime(socketKeepingTime);
            time = mPrinterManager.getSocketKeepingTime();

        } else {
            if(socketKeepingTime < 60000 || socketKeepingTime > 300000) {
                time = 300000;
            } else {
                time = socketKeepingTime;
            }
        }

        return time;
    }


    private int setInternationalCharacter(int internationalCharacter) {
        int international;
        if(mPrinterManager != null) {
            mPrinterManager.setInternationalCharacter(internationalCharacter);
            international = mPrinterManager.getInternationalCharacter();

        } else {
            international = internationalCharacter;
        }

        return international;
    }


    private int setCodePage(int codePage) {
        int code;
        if(mPrinterManager != null) {
            mPrinterManager.setCodePage(codePage);
            code = mPrinterManager.getCodePage();

        } else {
            code = codePage;
        }

        return code;
    }


    private String getInternationalCharacterString(int code) {
        String internationalCharacter;
        switch(code) {
            case PrinterManager.COUNTRY_USA:
                internationalCharacter = "USA";
                break;

            case PrinterManager.COUNTRY_FRANCE:
                internationalCharacter = "FRANCE";
                break;

            case PrinterManager.COUNTRY_GERMANY:
                internationalCharacter = "GERMANY";
                break;

            case PrinterManager.COUNTRY_ENGLAND:
                internationalCharacter = "ENGLAND";
                break;

            case PrinterManager.COUNTRY_DENMARK_1:
                internationalCharacter = "DENMARK I";
                break;

            case PrinterManager.COUNTRY_SWEDEN:
                internationalCharacter = "SWEDEN";
                break;

            case PrinterManager.COUNTRY_ITALY:
                internationalCharacter = "ITALY";
                break;

            case PrinterManager.COUNTRY_SPAIN:
                internationalCharacter = "SPAIN";
                break;

            case PrinterManager.COUNTRY_JAPAN:
                internationalCharacter = "JAPAN";
                break;

            case PrinterManager.COUNTRY_NORWAY:
                internationalCharacter = "NORWAY";
                break;

            case PrinterManager.COUNTRY_DENMARK_2:
                internationalCharacter = "DENMARK II";
                break;

            case PrinterManager.COUNTRY_SPAIN_2:
                internationalCharacter = "SPAIN II";
                break;

            case PrinterManager.COUNTRY_LATIN_AMERICA:
                internationalCharacter = "LATIN_AMERICA";
                break;

            case PrinterManager.COUNTRY_ARABIA:
                internationalCharacter = "ARABIA";
                break;

            default:
                internationalCharacter = "";
                break;
        }

        return internationalCharacter;
    }


    private String getCodePageString(int code) {
        String codePage;
        switch(code) {
            case PrinterManager.CODE_PAGE_KATAKANA:
                codePage = "Katakana";
                break;

            case PrinterManager.CODE_PAGE_1252:
                codePage = "Code Page 1252";
                break;

            case PrinterManager.CODE_PAGE_864:
                codePage = "Code Page 864";
                break;

            case PrinterManager.CODE_PAGE_1250:
                codePage = "Code Page 1250";
                break;

            case PrinterManager.CODE_PAGE_1251:
                codePage = "Code Page 1251";
                break;

            case PrinterManager.CODE_PAGE_1253:
                codePage = "Code Page 1253";
                break;

            case PrinterManager.CODE_PAGE_1254:
                codePage = "Code Page 1254";
                break;

            default:
                codePage = "";
                break;
        }

        return codePage;
    }
}
