/*
 * Intel License Header Holder
 */
package com.intel.webrtc.conference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Information of the participant in the conference.
 */
public final class Participant {

    /**
     * Interface for observing participant events.
     */
    public interface ParticipantObserver {
        /**
         * Called upon the participant leaves the conference.
         */
        void onLeft();
    }

    /**
     * Id of this Participant instance.
     */
    public final String id;

    /**
     * Role of the participant.
     */
    public final String role;

    /**
     * User id of the participant.
     */
    public final String userId;
    private List<ParticipantObserver> observers;

    Participant(JSONObject participantInfo) throws JSONException {
        id = participantInfo.getString("id");
        role = participantInfo.getString("role");
        userId = participantInfo.getString("user");
    }

    /**
     * Add a ParticipantObserver.
     *
     * @param observer ParticipantObserver to be added.
     */
    public void addObserver(ParticipantObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(observer);
    }

    /**
     * Remove a ParticipantObserver.
     *
     * @param observer ParticipantObserver to be removed.
     */
    public void removeObserver(ParticipantObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    void onLeft() {
        if (observers != null) {
            for (ParticipantObserver observer : observers) {
                observer.onLeft();
            }
        }
    }
}
