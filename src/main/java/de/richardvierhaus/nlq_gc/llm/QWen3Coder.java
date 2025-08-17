package de.richardvierhaus.nlq_gc.llm;

public class QWen3Coder extends QWenOpenRouter {

    private static final QWen3Coder INSTANCE = new QWen3Coder();

    private QWen3Coder() {
        super("qwen/qwen3-coder");
    }

    public static QWen3Coder getInstance() {
        return INSTANCE;
    }

}