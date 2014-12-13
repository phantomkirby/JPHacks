package im.ene.androooid.jphacks.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

import jp.ne.docomo.smt.dev.aitalk.AiTalkTextToSpeech;
import jp.ne.docomo.smt.dev.aitalk.data.AiTalkSsml;

/**
 * Created by eneim on 12/14/14.
 */
public class TextToSpeechTask extends AsyncTask<Void, Void, byte[]> {

    private final String source;
    private final TextToSpeechCallback mCallback;
    private AiTalkTextToSpeech search;

    public TextToSpeechTask(String string, TextToSpeechCallback callback) {
        super();
        this.source = string;
        this.search = new AiTalkTextToSpeech();
        this.mCallback = callback;
    }

    @Override
    protected byte[] doInBackground(Void... params) {
        AiTalkSsml ssml = null;
        try {
            ssml = new AiTalkSsml();
            ssml.startVoice("nozomi");
            ssml.addText(this.source);
            ssml.endVoice();

            return search.requestAiTalkSsmlToSound(ssml.makeSsml());
        } catch (Exception er) {
            er.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return;

        // 音声出力用バッファ作成
        int bufSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // ビッグエディアンをリトルエディアンに変換
        search.convertByteOrder16(bytes);
        // 音声出力
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);

        if (mCallback != null)
            mCallback.onAudioCallback(at);
    }

    public interface TextToSpeechCallback {
        void onAudioCallback(AudioTrack track);
    }
}
