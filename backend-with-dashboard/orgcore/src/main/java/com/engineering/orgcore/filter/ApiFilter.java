package com.engineering.orgcore.filter;


import com.engineering.orgcore.entity.ApiLogEntity;
import com.engineering.orgcore.repository.ApiLogRepository;
import com.engineering.orgcore.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rayyan
 * @version 1.0
 */
@Slf4j
@Component
public class ApiFilter extends OncePerRequestFilter {

    private final ApiLogRepository apiLogRepository;
    private final JwtUtil jwtUtil;
    private static final int MAX_DB_LENGTH = 3900;


    public ApiFilter(final ApiLogRepository apiLogRepository, final JwtUtil jwtUtil) {
        this.apiLogRepository = apiLogRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

//        for (AuditSkipList skipList : AuditSkipList.values()) {
//            if (request.getRequestURI().startsWith(skipList.getUri())) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//        }

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }

    }//doFilterInternal

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain) throws ServletException, IOException {

        long requestNanoTime = System.nanoTime();

        String queryParameters = null;
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            queryParameters = request.getQueryString();
        }

        String requestData = "";
        String responseData = "";
        try {
            filterChain.doFilter(request, response);
        } finally {

            byte[] requestContent = request.getContentAsByteArray();
            if (requestContent.length > 0) {
                requestData = new String(requestContent, request.getCharacterEncoding());
            }
            String requestHeaders = getRequestHeaders(request);
            if (requestHeaders != null) {
                requestData = !requestData.isEmpty() ? "{\"body\":" + requestData + ",\n\"headers\": " + requestHeaders + "}" : "\"headers\"" + requestHeaders;
            }

            byte[] responseContent = response.getContentAsByteArray();
            if (responseContent.length > 0) {
                responseData = new String(responseContent, request.getCharacterEncoding());
            }
            String responseHeaders = getResponseHeaders(response);
            if (responseHeaders != null) {
                responseData = !responseData.isEmpty() ? "{\"body\":" + responseData + ",\n\"headers\": " + responseHeaders + "}" : "\"headers\"" + responseHeaders;
            }

            response.copyBodyToResponse();
        }

        int elapsedType = 3;
        long endNanoTime = System.nanoTime();
        long elapsedTime = (endNanoTime - requestNanoTime) / 1000000;
        Long traceId = null;
        try {
          //  traceId = jwtUtil.getTokenInfo(request).getTraceId();
        } catch (Exception e) {
            //Do Nothing
        }

        ApiLogEntity apiLogEntity = new ApiLogEntity();
       // apiLogEntity.setLlId(traceId);
        apiLogEntity.setApiType(request.getMethod());
        apiLogEntity.setApiName(request.getRequestURI());
        apiLogEntity.setCreatedAt(LocalDateTime.now());
        apiLogEntity.setCreatedBy(request.getRemoteUser());
        apiLogEntity.setQueryParameters(queryParameters);
        apiLogEntity.setRequestData(safeForDb(requestData));
        apiLogEntity.setResponseData(safeForDb(responseData));
        apiLogEntity.setElapsedType(elapsedType);
        apiLogEntity.setElapsedTime(elapsedTime);
        apiLogEntity.setResultCode(1L);
        apiLogEntity.setResultDesc("success");

        apiLogRepository.save(apiLogEntity);

    }//doFilterWrapped

    private static String getRequestHeaders(ContentCachingRequestWrapper request) throws JsonProcessingException {

        Map<String, String> responseParameters = new TreeMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            responseParameters.put(paramName, paramValue);
        }

        return new ObjectMapper().writeValueAsString(responseParameters);
    }//getHeaders

    private static String getResponseHeaders(ContentCachingResponseWrapper response) throws JsonProcessingException {

        Map<String, String> responseParameters = new TreeMap<>();
        Collection<String> parameterNames = response.getHeaderNames();
        for (String key : parameterNames) {
            responseParameters.put(key, response.getHeader(key));
        }

        return new ObjectMapper().writeValueAsString(responseParameters);
    }//getHeaders

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper contentCachingRequestWrapper) {
            return contentCachingRequestWrapper;
        } else {
            return new ContentCachingRequestWrapper(request,10240); // 10KB cache
        }
    }//wrapRequest

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper contentCachingResponseWrapper) {
            return contentCachingResponseWrapper;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }//wrapResponse

    private static String safeForDb(String value) {
        if (value == null) {
            return null;
        }

        if (value.length() > MAX_DB_LENGTH) {
            return value.substring(0, MAX_DB_LENGTH) + "...[TRUNCATED]";
        }

        return value;
    }

}//ApiFilter
