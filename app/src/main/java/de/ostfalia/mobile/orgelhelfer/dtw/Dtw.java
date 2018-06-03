package de.ostfalia.mobile.orgelhelfer.dtw;

import android.view.KeyEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiNote;

public class Dtw implements MidiDataManager.OnMidiDataListener {
    public static final int RECORDINGBUFFERSIZE = 100;
    public static final int STREAMBUFFERSIZE = 100;
    public static final int DELTAREACTIONTIME = 20;
    public Recording<MidiEvent> rec;
    public EventScheduler<MidiEvent> scheduler;
    public StreamBuffer<MidiEvent> streamBuffer;
    public float[][] matrix;
    int totalStreamedElements;
    int minIndex;

    public Dtw(ArrayList<MidiEvent> recordingList, ArrayList<MidiEvent> talkingEvents) {
        rec = new Recording<>(recordingList, RECORDINGBUFFERSIZE);
        scheduler = new EventScheduler<>(this, talkingEvents);
        streamBuffer = new StreamBuffer<>(STREAMBUFFERSIZE);
        matrix = new float[STREAMBUFFERSIZE][RECORDINGBUFFERSIZE];
        MidiDataManager.getInstance().addOnMidiDataListener(this);
    }

    public static Dtw constructDTW(List<MidiEvent> recordingList) {
        ArrayList<MidiEvent> retRecordingList = new ArrayList<>();
        ArrayList<MidiEvent> retTalkingList = new ArrayList<>();
        for (int i = 0; i < recordingList.size(); i++) {
            //if(recordingList.get(i).getmType() == MidiConstants.MessageTypes.STATUS_PROGRAM_CHANGE.getType() || recordingList.get(i).getmType() == MidiConstants.MessageTypes.STATUS_CONTROL_CHANGE.getType()) {
            if (recordingList.get(i).getmType() == MidiConstants.MessageTypes.STATUS_NOTE_ON.getType()) {
                    //&& recordingList.get(i).getRaw()[1] < 59) {

                retTalkingList.add(recordingList.get(i));
                retRecordingList.add(recordingList.get(i));
            } /*else {
                if (recordingList.get(i).getmType() == MidiConstants.MessageTypes.STATUS_NOTE_ON.getType()) {
                    //&& recordingList.get(i).getRaw()[2] > 0)
                    retRecordingList.add(recordingList.get(i));
                }
            }*/
        }
        return new Dtw(retRecordingList, retTalkingList);
    }

    public void next(MidiEvent event) {
        if (!scheduler.hasStarted()) {
            scheduler.start(rec.get(0).getTimestamp());
        }
        streamBuffer.addElement(event);
        totalStreamedElements++;
        for (int i = 0; i < rec.getCurrentBufferSize(); i++) {
            matrix[streamBuffer.getCurrentIndex()][i] = event.dtwCompareTo(rec.get(i)) + min(neighboursOf(streamBuffer.getCurrentIndex(), i));
        }
        //TODO: WAS ist wenn es mehrere minIndexe gibt?
        if (rec.lookForBetterEvents(event, minIndex + 1)) {
            redoMatrix();
        }
        minIndex = minIndexOfCol(streamBuffer.getCurrentIndex());

        //new Auskommentiert
        //scheduler.setTime(rec.get(minIndex).getTimestamp());

        if (minIndex > (int) (RECORDINGBUFFERSIZE * (3f / 4f))) {
            moveMatrix((int) (RECORDINGBUFFERSIZE * (1f / 4f)));
        }

        //new Hinzugefügt
        minIndex = minIndexOfCol(streamBuffer.getCurrentIndex());
    }

    public void redoMatrix() {
        int x = streamBuffer.getLastIndex();
        for (int i = 0; i < streamBuffer.getCurrentBufferSize(); i++) {
            for (int n = 0; n < rec.getCurrentBufferSize(); n++) {
                if (i != 0 && n != 0) {
                    matrix[(x + i) % streamBuffer.getCurrentBufferSize()][n] = streamBuffer.get((x + i)).dtwCompareTo(rec.get(n)) + min(neighboursOf(x + i, n));
                }
            }
        }
    }

    private void moveMatrix(int amount) {
        System.out.println("------------MATRIX VERSCHOBEN UM: " + amount + "------------");
        rec.moveCurrentSnippet(amount);
        float[][] newMatrix = matrix.clone();
        for (int i = 0; i < streamBuffer.getCurrentBufferSize(); i++) {
            for (int n = 0; n + amount < rec.getCurrentBufferSize(); n++) {
                newMatrix[i][n] = matrix[i][n + amount];
            }
            for (int n = rec.getCurrentBufferSize() - amount; n < rec.getCurrentBufferSize(); n++) {
                newMatrix[i][n] = streamBuffer.get(i).dtwCompareTo(rec.get(n)) + min(neighboursOf(i, n));
            }
        }
        this.matrix = newMatrix;
    }

    public int minIndexOfCol(int col) {
        float minValue = Float.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < rec.getCurrentBufferSize(); i++) {
            if (matrix[col][i] <= minValue) {
                minValue = matrix[col][i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    public ArrayList<Float> neighboursOf(int x, int y) {
        ArrayList<Float> neighbours = new ArrayList<Float>(4);

        if (x > 0 && x != streamBuffer.getCurrentIndex() + 1) {

            neighbours.add(matrix[x - 1][y] + 0.2f);
            if (y > 0) {
                neighbours.add(matrix[x - 1][y - 1]);
            }
        } else if (x == 0 && totalStreamedElements > STREAMBUFFERSIZE) {
            neighbours.add(matrix[matrix.length - 1][y] + 0.2f);
            if (y > 0) {
                neighbours.add(matrix[matrix.length - 1][y - 1]);
            }
        }

        if (y > 0) {
            neighbours.add(matrix[x][y - 1] + 0.2f);
        }
        return neighbours;
    }

    public float min(ArrayList<Float> list) {
        float min = Float.MAX_VALUE;
        if (list.size() == 0) {
            return 0;
        } else {
            for (Float f : list) {
                if (min > f) {
                    min = f;
                }
            }
        }
        return min;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append((char) 9);
        sb.append(streamBuffer.toString());
        sb.append("\n");
        for (int i = 0; i < rec.getCurrentBufferSize(); i++) {
            sb.append(rec.get(i).toString());
            sb.append((char) 9);
            for (int n = 0; n < streamBuffer.getCurrentBufferSize(); n++) {
                //sb.append(matrix[n][i]);
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                sb.append(df.format(matrix[n][i]));
                sb.append((char) 9);
                if (n == streamBuffer.getCurrentIndex()) {
                    sb.append("|");
                    sb.append((char) 9);
                }
            }
            sb.append("\n");
        }
        sb.append(totalStreamedElements + "," + (minIndexOfCol(streamBuffer.getCurrentIndex()) + rec.offset + 1));
        return sb.toString();
    }

    @Override
    public void onMidiData(MidiEvent event) {
        next(event);
        scheduler.setTime(rec.get(minIndex).getTimestamp());
    }
}
