package ch.amana.android.cputuner.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CpuFrequencyChooser;
import ch.amana.android.cputuner.helper.CpuFrequencyChooser.FrequencyChangeCallback;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper.GovernorConfig;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.view.fragments.GovernorBaseFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragmentCallback;
import ch.amana.android.cputuner.view.fragments.VirtualGovernorFragment;

public class ProfileEditor extends FragmentActivity implements GovernorFragmentCallback, FrequencyChangeCallback {

	private ProfileModel profile;
	private CpuHandler cpuHandler;
	private SeekBar sbCpuFreqMax;
	private Spinner spCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private Spinner spCpuFreqMin;
	private int[] availCpuFreqsMax;
	private int[] availCpuFreqsMin;
	private Spinner spWifi;
	private Spinner spGps;
	private Spinner spBluetooth;
	private TextView labelCpuFreqMin;
	private TextView labelCpuFreqMax;
	private EditText etName;
	private Spinner spMobileData3G;
	private Spinner spSync;
	private boolean hasDeviceStatesBeta;
	private Spinner spMobileDataConnection;
	// private LinearLayout llTop;
	private GovernorBaseFragment governorFragment;
	private TableRow trMinFreq;
	private TableRow trMaxFreq;
	private Spinner spAirplaneMode;
	private CpuFrequencyChooser cpuFrequencyChooser;
	private boolean save;
	private ModelAccess modelAccess;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_editor);

		modelAccess = ModelAccess.getInstace(this);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			profile = modelAccess.getProfile(getIntent().getData());
		}
		// TODO remove -- we should never get there
		// else if (Intent.ACTION_EDIT.equals(action)) {
		// profile = CpuHandler.getInstance().getCurrentCpuSettings();
		// origProfile = CpuHandler.getInstance().getCurrentCpuSettings();
		// }

		if (profile == null) {
			profile = new ProfileModel();
		}

		setTitle(getString(R.string.title_profile_editor) + " " + profile.getProfileName());

		cpuHandler = CpuHandler.getInstance();
		availCpuFreqsMax = cpuHandler.getAvailCpuFreq(false);
		availCpuFreqsMin = cpuHandler.getAvailCpuFreq(true);

		SettingsStorage settings = SettingsStorage.getInstance();

		if (settings.isUseVirtualGovernors()) {
			governorFragment = new VirtualGovernorFragment(this, profile);
		} else {
			governorFragment = new GovernorFragment(this, profile);
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.llGovernorFragmentAncor, governorFragment);
		fragmentTransaction.commit();

		if (profile.getMinFreq() < cpuHandler.getMinimumSensibleFrequency() && settings.isBeginnerUser()) {
			if (availCpuFreqsMin != null && availCpuFreqsMin.length > 0) {
				profile.setMinFreq(availCpuFreqsMin[0]);
			}
		}

		if (ProfileModel.NO_VALUE_INT == profile.getMinFreq() && availCpuFreqsMin.length > 0) {
			profile.setMinFreq(cpuHandler.getMinCpuFreq());
		}
		if (ProfileModel.NO_VALUE_INT == profile.getMaxFreq() && availCpuFreqsMax.length > 0) {
			profile.setMaxFreq(cpuHandler.getMaxCpuFreq());
		}

		// TODO remove?
		hasDeviceStatesBeta = 3 == Math.max(profile.getWifiState(),
				Math.max(profile.getGpsState(),
						Math.max(profile.getMobiledata3GState(),
								Math.max(profile.getBluetoothState(),
										Math.max(profile.getBackgroundSyncState(),
												profile.getWifiState())))));

		// llTop = (LinearLayout) findViewById(R.id.llTop);
		etName = (EditText) findViewById(R.id.etName);
		spCpuFreqMax = (Spinner) findViewById(R.id.spCpuFreqMax);
		spCpuFreqMin = (Spinner) findViewById(R.id.spCpuFreqMin);
		labelCpuFreqMin = (TextView) findViewById(R.id.labelCpuFreqMin);
		labelCpuFreqMax = (TextView) findViewById(R.id.labelCpuFreqMax);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);
		spWifi = (Spinner) findViewById(R.id.spWifi);
		spGps = (Spinner) findViewById(R.id.spGps);
		spBluetooth = (Spinner) findViewById(R.id.spBluetooth);
		spMobileData3G = (Spinner) findViewById(R.id.spMobileData3G);
		spMobileDataConnection = (Spinner) findViewById(R.id.spMobileDataConnection);
		spAirplaneMode = (Spinner) findViewById(R.id.spAirplaneMode);
		spSync = (Spinner) findViewById(R.id.spSync);
		trMaxFreq = (TableRow) findViewById(R.id.TableRowMaxFreq);
		trMinFreq = (TableRow) findViewById(R.id.TableRowMinFreq);


		cpuFrequencyChooser = new CpuFrequencyChooser(this, sbCpuFreqMin, spCpuFreqMin, sbCpuFreqMax, spCpuFreqMax);

		TableLayout tlServices = (TableLayout) findViewById(R.id.TableLayoutServices);
		if (settings.isEnableSwitchWifi()) {
			spWifi.setAdapter(getSystemsAdapter());
			spWifi.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setWifiState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowWifi));
		}

		if (settings.isEnableSwitchGps()) {
			spGps.setAdapter(getSystemsAdapter());
			spGps.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setGpsState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowGps));
		}

		if (settings.isEnableSwitchBluetooth()) {
			spBluetooth.setAdapter(getSystemsAdapter());
			spBluetooth.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setBluetoothState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowBluetooth));
		}

		if (settings.isEnableSwitchMobiledata3G()) {
			int mobiledatastates = R.array.mobiledataStates;
			if (settings.isEnableBeta()) {
				mobiledatastates = R.array.mobiledataStatesBeta;
			}
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, mobiledatastates, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spMobileData3G.setAdapter(adapter);
			spMobileData3G.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setMobiledata3GState(pos);

				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowMobileData3G));
		}

		if (settings.isEnableSwitchMobiledataConnection()) {
			spMobileDataConnection.setAdapter(getSystemsAdapter());
			spMobileDataConnection.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setMobiledataConnectionState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowMobiledataConnection));
		}

		if (settings.isEnableSwitchBackgroundSync()) {
			spSync.setAdapter(getSystemsAdapter());
			spSync.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setBackgroundSyncState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowSync));
		}

		if (settings.isEnableAirplaneMode()) {
			spAirplaneMode.setAdapter(getSystemsAdapter());
			spAirplaneMode.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setAirplainemodeState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowAirplaneMode));
		}
		// hide keyboard
		etName.setInputType(InputType.TYPE_NULL);
		etName.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				etName.setInputType(InputType.TYPE_CLASS_TEXT);
				return false;
			}
		});

		// updateView();
	}

	private ArrayAdapter<CharSequence> getSystemsAdapter() {
		int devicestates = R.array.deviceStates;
		if (SettingsStorage.getInstance().isEnableBeta() || hasDeviceStatesBeta) {
			devicestates = R.array.deviceStatesBeta;
		}
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, devicestates, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		updateModel();
		profile.saveToBundle(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void updateModel() {
		profile.setProfileName(etName.getText().toString());
		governorFragment.updateModel();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (profile == null) {
			profile = new ProfileModel(savedInstanceState);
		} else {
			profile.readFromBundle(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		save = true;
		updateView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateModel();
		try {
			String action = getIntent().getAction();
			if (Intent.ACTION_INSERT.equals(action)) {
				if (save) {
					modelAccess.insertProfile(profile);
				}

			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (save) {
					modelAccess.updateProfile(profile);
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);

		}
	}

	@Override
	public void updateView() {
		String profileName = profile.getProfileName();
		if (!ProfileModel.NO_VALUE_STR.equals(profileName)) {
			etName.setText(profileName);
		}
		cpuFrequencyChooser.setMaxCpuFreq(profile.getMaxFreq());
		cpuFrequencyChooser.setMinCpuFreq(profile.getMinFreq());
		spWifi.setSelection(profile.getWifiState());
		spGps.setSelection(profile.getGpsState());
		spBluetooth.setSelection(profile.getBluetoothState());
		spMobileData3G.setSelection(profile.getMobiledata3GState());
		spMobileDataConnection.setSelection(profile.getMobiledataConnectionState());
		spSync.setSelection(profile.getBackgroundSyncState());
		spAirplaneMode.setSelection(profile.getAirplainemodeState());

		GovernorConfig governorConfig = GovernorConfigHelper.getGovernorConfig(profile.getGov());
		if (governorConfig.hasNewLabelCpuFreqMax()) {
			labelCpuFreqMax.setText(governorConfig.getNewLabelCpuFreqMax(this));
		} else {
			labelCpuFreqMax.setText(R.string.labelMax);
		}
		if (governorConfig.hasMinFrequency()) {
			GuiUtils.showViews(trMinFreq, new View[] { labelCpuFreqMin, spCpuFreqMin, sbCpuFreqMin });
		} else {
			GuiUtils.hideViews(trMinFreq, new View[] { labelCpuFreqMin, spCpuFreqMin, sbCpuFreqMin });
		}
		if (governorConfig.hasMaxFrequency()) {
			GuiUtils.showViews(trMaxFreq, new View[] { labelCpuFreqMax, spCpuFreqMax, sbCpuFreqMax });
		} else {
			GuiUtils.hideViews(trMaxFreq, new View[] { labelCpuFreqMax, spCpuFreqMax, sbCpuFreqMax });
		}
		governorFragment.updateView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.edit_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemCancel:
			save = false;
			finish();
			return true;

		case R.id.menuItemSave:
			finish();
			return true;

		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_PROFILE)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void setMaxCpuFreq(int val) {
		profile.setMaxFreq(val);
	}

	@Override
	public void setMinCpuFreq(int val) {
		profile.setMinFreq(val);
	}
}
