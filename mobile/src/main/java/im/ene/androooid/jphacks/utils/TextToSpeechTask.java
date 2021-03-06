package im.ene.androooid.jphacks.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

import jp.ne.docomo.smt.dev.aitalk.AiTalkTextToSpeech;
import jp.ne.docomo.smt.dev.aitalk.data.AiTalkSsml;

/**
 * Created by eneim on 12/14/14.
 */
public class TextToSpeechTask extends AsyncTask<Void, Void, byte[]> {

    private final String source;
    private AiTalkTextToSpeech search;

    public TextToSpeechTask(String string) {
        super();
        this.source = string;
        this.search = new AiTalkTextToSpeech();
    }

    @Override
    protected byte[] doInBackground(Void... params) {
        AiTalkSsml ssml = null;
        try {
            ssml = new AiTalkSsml();
            ssml.startVoice("nozomi");
            ssml.addText(this.source);
            ssml.endVoice();

            byte[] data = search.requestAiTalkSsmlToSound(ssml.makeSsml());

            Log.d("AITalk API", "" + data.length);

            int bufSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            // ビッグエディアンをリトルエディアンに変換
            search.convertByteOrder16(data);
            // 音声出力
            AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);

            at.play();
            at.write(data, 0, data.length);
            // 音声出力待ち
            try {
                Thread.sleep(data.length / 32);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return data;
        } catch (Exception er) {
            er.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        return;
    }
}
