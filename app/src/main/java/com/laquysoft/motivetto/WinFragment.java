package com.laquysoft.motivetto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class WinFragment extends Fragment implements OnClickListener {
    String mExplanation = "";
    int mScore = 0;
    boolean mShowSignIn = false;

    public interface Listener {
        public void onWinScreenDismissed();
        public void onWinScreenSignInClicked();
    }

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_win, container, false);
        v.findViewById(R.id.win_ok_button).setOnClickListener(this);
        return v;
    }

    public void setFinalScore(int i) {
        mScore = i;
    }

    public void setExplanation(String s) {
        mExplanation = s;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView scoreTv = (TextView) getActivity().findViewById(R.id.score_display);
        TextView explainTv = (TextView) getActivity().findViewById(R.id.scoreblurb);

        if (scoreTv != null) scoreTv.setText(String.valueOf(mScore));
        if (explainTv != null) explainTv.setText(mExplanation);

    }

    @Override
    public void onClick(View view) {
        MediaPlayerService.pauseTrack(getActivity());
        mListener.onWinScreenDismissed();
    }

}
