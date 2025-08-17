package de.richardvierhaus.nlq_gc.llm;

public class QWen3_235bFree extends QWenOpenRouter {

    private static final QWen3_235bFree INSTANCE = new QWen3_235bFree();

    private QWen3_235bFree() {
        super("qwen/qwen3-235b-a22b:free");
    }

    public static QWen3_235bFree getInstance() {
        return INSTANCE;
    }

}