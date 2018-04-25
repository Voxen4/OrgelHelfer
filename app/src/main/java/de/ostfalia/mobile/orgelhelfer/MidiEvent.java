package de.ostfalia.mobile.orgelhelfer;

/**
 * Created by kellerm on 23.04.2018.
 * Class representing a interpreted MidiKey Event, contains The Key Pressed and the "strength"?
 */

enum MidiEvent {
    KEY("Hi");

    private final String mUrl;

    MidiEvent(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    /**
     * Get the enum from the url string.
     *
     * @param _url url string to find
     * @return enum value, if found, else null
     */
    public static MidiEvent getByUrl(final String _url) {
        for (MidiEvent url : values()) {
            if (url.getUrl().equals(_url)) {
                return url;
            }
        }
        return null;
    }
}

enum NoteOn{
    //90 2D 1D: NoteOn(1, 45, 29)
    MIDDLEC((byte)(0x90 + 2),(byte)60,(byte)127);

    private final byte channel; //MIDI channels 1-16 are encoded as 0-15.
    private final byte pitch;
    private final byte velocity; // between 0 and 127

    NoteOn(byte _channel,byte _pitch,byte _velocity) {
        channel = _channel;
        pitch = _pitch;
        velocity = _velocity;
    }

    public byte getChannel() {
        return channel;
    }

    public byte getPitch() {
        return pitch;
    }
    public byte getVelocity() {
        return velocity;
    }

}
