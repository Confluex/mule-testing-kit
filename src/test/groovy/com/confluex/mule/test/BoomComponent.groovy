package com.confluex.mule.test

import org.mule.api.MuleEventContext
import org.mule.api.lifecycle.Callable

class BoomComponent implements Callable{
    @Override
    Object onCall(MuleEventContext eventContext) throws Exception {
        throw new RuntimeException("BOOM")
    }
}
