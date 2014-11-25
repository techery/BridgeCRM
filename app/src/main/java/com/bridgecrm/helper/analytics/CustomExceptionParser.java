package com.bridgecrm.helper.analytics;


import com.google.android.gms.analytics.ExceptionParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


public class CustomExceptionParser implements ExceptionParser {

    private static final int MAX_COUNT_LINES = 10;

    @Override
    public String getDescription(String threadName, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println(throwable.toString());
        try {
            addCauseInfo(printWriter, throwable.getCause().getStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "Thread:" + threadName + "\n Exception: " + stringWriter.toString() + " \n";
    }

    private void addCauseInfo(Appendable err, StackTraceElement[] stack) throws IOException {
        int countLines = stack.length > MAX_COUNT_LINES ? MAX_COUNT_LINES : stack.length;

        for (int i = 0; i < countLines; i++) {
            err.append("\tat ");
            err.append(stack[i].toString());
            err.append("\n");
        }
        err.append("\t…");
    }
}
