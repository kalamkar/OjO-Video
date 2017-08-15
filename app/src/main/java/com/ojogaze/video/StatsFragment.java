package com.ojogaze.video;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import care.dovetail.ojo.EyeEvent;
import care.dovetail.ojo.Gesture;
import care.dovetail.ojo.bluetooth.EogDevice;

/**
 * Created by abhi on 4/24/17.
 */

public class StatsFragment extends Fragment implements Gesture.Observer, EogDevice.Observer,
        EyeEvent.Observer {

    private TextView leftFixations;
    private TextView rightFixations;

    private TextView leftSaccades;
    private TextView rightSaccades;

    private TextView leftBlinkRate;
    private TextView rightBlinkRate;

    private int blinks;
    private int fixations;
    private int saccades;
    private long startTimeMillis;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leftFixations = (TextView) view.findViewById(R.id.leftFixations);
        rightFixations = (TextView) view.findViewById(R.id.rightFixations);

        leftSaccades = (TextView) view.findViewById(R.id.leftSaccades);
        rightSaccades = (TextView) view.findViewById(R.id.rightSaccades);

        leftBlinkRate = (TextView) view.findViewById(R.id.leftBlinkRate);
        rightBlinkRate = (TextView) view.findViewById(R.id.rightBlinkRate);
    }

    @Override
    public void setEyeEventSource(EyeEvent.Source eyeEventSource) {
        eyeEventSource.add(new Gesture("blink")
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.UP, 2000))
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.DOWN, 4000))
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.UP, 2000))
                .addObserver(this));
        eyeEventSource.add(new Gesture("long_fixation")
                .add(EyeEvent.Criterion.fixation(1000, 1500))
                .addObserver(this));
        eyeEventSource.add(this);
    }

    @Override
    public EyeEvent.Criteria getCriteria() {
        return new EyeEvent.AnyCriteria()
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.LEFT, 2000))
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.RIGHT, 2000))
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.UP, 2000))
                .add(EyeEvent.Criterion.saccade(EyeEvent.Direction.DOWN, 2000));
    }

    @Override
    public void onEyeEvent(final EyeEvent eyeEvent) {
        Activity activity = getActivity();
        if (activity == null || activity.isDestroyed() || activity.isRestricted()
                || activity.isFinishing()) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long timeMillis = System.currentTimeMillis() - startTimeMillis;
                timeMillis = timeMillis == 0 ? 1 : timeMillis;
                if (eyeEvent.type == EyeEvent.Type.SACCADE) {
                    saccades++;
                    float mins = timeMillis / (1000 * 60);
                    mins = mins == 0 ? 1 : mins;
                    float saccadesRate = saccades / mins;
                    leftSaccades.setText(String.format("%.1f", saccadesRate));
                    rightSaccades.setText(String.format("%.1f", saccadesRate));
                }
            }
        });
    }

    @Override
    public void onGesture(final String gestureName, final List<EyeEvent> events) {
        Activity activity = getActivity();
        if (activity == null || activity.isDestroyed() || activity.isRestricted()
                || activity.isFinishing()) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long timeMillis = System.currentTimeMillis() - startTimeMillis;
                timeMillis = timeMillis == 0 ? 1 : timeMillis;
                switch (gestureName) {
                    case "blink":
                        blinks++;
                        float mins = timeMillis / (1000 * 60);
                        mins = mins == 0 ? 1 : mins;
                        float blinkRate = blinks / mins;
                        leftBlinkRate.setText(String.format("%.1f", blinkRate));
                        rightBlinkRate.setText(String.format("%.1f", blinkRate));
                        break;
                    case "long_fixation":
                        fixations++;
                        leftFixations.setText(Integer.toString(fixations));
                        rightFixations.setText(Integer.toString(fixations));
                        break;
                }
            }
        });
    }

    @Override
    public void onConnect(String s) {
        startTimeMillis = System.currentTimeMillis();
        blinks = 0;
        fixations = 0;
        setProgressVisibility(View.INVISIBLE);
    }

    @Override
    public void onDisconnect(String s) {
        setProgressVisibility(View.VISIBLE);
    }

    @Override
    public void onNewValues(int i, int i1) {
    }

    private void setProgressVisibility(final int visibility) {
        Activity activity = getActivity();
        if (activity == null || activity.isDestroyed() || activity.isRestricted()
                || activity.isFinishing()) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView().findViewById(R.id.leftProgress).setVisibility(visibility);
                getView().findViewById(R.id.rightProgress).setVisibility(visibility);
            }
        });
    }
}
