package com.clubix.api.tools.formatter;

import com.usecase.model.response.Response;

public interface ResponseFormatter<R extends Response> {
    String format(R response);
}

