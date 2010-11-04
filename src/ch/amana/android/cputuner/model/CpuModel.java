package ch.amana.android.cputuner.model;

import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;

public class CpuModel {

	public static final String INTENT_EXTRA = "CpuModelData";
	public static final String NO_PROFILE = "None";

	private String gov;
	private String maxFreq;
	private String minFreq;
	private String profile = NO_PROFILE;

	public CpuModel(String gov, String maxFreq, String minFreq) {
		super();
		this.gov = gov;
		this.maxFreq = maxFreq;
		this.minFreq = minFreq;
	}

	public CpuModel(String profile) {
		super();
		SettingsStorage store = SettingsStorage.getInstance();
		store.dumpPerferences();
		this.profile = profile;
		this.gov = getFromStore(store, profile, CpuHandler.SCALING_GOVERNOR);
		this.maxFreq = getFromStore(store, profile, CpuHandler.SCALING_MAX_FREQ);
		this.minFreq = getFromStore(store, profile, CpuHandler.SCALING_MIN_FREQ);
	}

	private String getFromStore(SettingsStorage store, String profile, String key) {
		String value = store.getValue(profile + "_" + key);
		if (value.equals(SettingsStorage.NO_VALUE)) {
			value = store.getValue(key);
		}
		return value;
	}

	public void save() {
		if (NO_PROFILE.equals(NO_PROFILE)) {
			Log.w(Logger.TAG, "Not saving since profile is " + NO_PROFILE);
		}
		SettingsStorage store = SettingsStorage.getInstance();
		store.writeValue(profile + "_" + CpuHandler.SCALING_GOVERNOR, gov);
		store.writeValue(profile + "_" + CpuHandler.SCALING_MAX_FREQ, maxFreq);
		store.writeValue(profile + "_" + CpuHandler.SCALING_MIN_FREQ, minFreq);
	}

	public String getGov() {
		return gov;
	}

	public void setGov(String gov) {
		this.gov = gov;
	}

	public String getMaxFreq() {
		return maxFreq;
	}

	public void setMaxFreq(String maxFreq) {
		this.maxFreq = maxFreq;
	}

	public String getMinFreq() {
		return minFreq;
	}

	public void setMinFreq(String minFreq) {
		this.minFreq = minFreq;
	}

	public CharSequence getProfileName() {
		return profile;
	}

}
