package ch.amana.android.cputuner.view.activity;

import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.R.layout;
import ch.amana.android.cputuner.helper.SettingsStorage;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UserExperianceLevelChooser extends Dialog {

	private RadioGroup rgUserLevel;
	private SettingsStorage settingsStorage;

	public UserExperianceLevelChooser(Context context) {
	    super(context);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
	             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

	    setTitle("Choose your experiance level");
	    setContentView(R.layout.user_experiance_level_chooser);
	    // TODO Auto-generated method stub
	    rgUserLevel =  (RadioGroup)findViewById(R.id.rgUserlevel);
	    int userLevel = R.id.rbNormal;
	    settingsStorage = SettingsStorage.getInstance();
		if (settingsStorage.isPowerUser()) {
	    	userLevel = R.id.rbPowerUser;
	    }else if (settingsStorage.isBeginnerUser()) {
	    	userLevel = R.id.rbBeginner;
	    }
		rgUserLevel.check(userLevel);
		
		((Button)findViewById(R.id.buOk)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int userLevel = 2;
				int checkedId = rgUserLevel.getCheckedRadioButtonId();
				if (checkedId == R.id.rbPowerUser) {
					userLevel = 3;
			    }else if (checkedId == R.id.rbBeginner) {
			    	userLevel = 1;
			    }
				settingsStorage.setUserLevel(userLevel);
				UserExperianceLevelChooser.this.dismiss();
				
			}
		});

		
		((Button)findViewById(R.id.buCancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserExperianceLevelChooser.this.cancel();
			}
		});
	}

}
