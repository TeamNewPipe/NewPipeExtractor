package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

public class YoutubeMusicCheckBody extends YoutubeCheckBody {
    private final String input = "";

    public YoutubeMusicCheckBody() {
        super(new YoutubeMusicContext());
    }

    @Override
    public String toString() {
        return "YoutubeMusicCheckBody{input='" + input + "'} " + super.toString();
    }
}
