function formatTimestamp(timestamp) {
    var dateObj = new Date(timestamp);

    var year = dateObj.getYear() + 1900;
    var month = dateObj.getMonth() + 1;
    var theDate = dateObj.getDate();
    var hour = dateObj.getHours();
    var minute = dateObj.getMinutes();
    var second = dateObj.getSeconds();
    return year + "-" + month + "-" + theDate + " " + hour + ":" + minute + ":" + second;
}

function formatXml(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function (index, node) {
        var indent = 0;
        if (node.match(/.+<\/\w[^>]*>$/)) {
            indent = 0;
        } else if (node.match(/^<\/\w/)) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
            indent = 1;
        } else {
            indent = 0;
        }
        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }
        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
}

function formatJson(ugly) {
    if (ugly !== '') {
        try {
            var obj = JSON.parse(ugly);
            return JSON.stringify(obj, undefined, 4);
        } catch (e) {
            alert("JSON字符串错误，请检查");
            return ugly;
        }
    }
}

function initBaseInput(doc) {
    // 设定method
    $("#method").find("option[value=" + doc['method'] + "]").attr("selected", true);

    // 设定api地址
    $('#url').val(doc['url']);

    // 设定api名称
    $('#apiName').val(doc['name']);

    // 检测URL
    $.ajax({
        method: "POST",
        url: "/restdoc/httpstandard/helper/url/var/extract",
        data: JSON.stringify({url: doc['url']}),
        contentType: 'application/json',
        success: function (res) {
            if (res.code === '200') {
                if (Object.keys(res.data).length > 0) {
                    initUriFieldDoc(res.data, oneuriline)
                }
            }
        }
    });
}

function projectToJson(data) {
    var json = {};
    $.ajax({
        method: "POST",
        url: "/restdoc/textprotocol/serialize2Json",
        contentType: 'application/json',
        data: data,
        async: false,
        success: function (res) {
            if (res.code === '200') {
                json = res.data;
            } else {
                alert(res.message);
            }
        }
    });
    return json;
}

function projectToXml(data) {
    var xml = "";
    $.ajax({
        method: "POST",
        url: "/restdoc/textprotocol/serialize2Xml",
        contentType: 'application/json',
        async: false,
        data: data,
        success: function (res) {
            if (res.code === '200') {
                xml = res.data;
            } else {
                alert(res.message);
            }
        }
    });
    return xml;
}

function initTestApiDoc(testLog, doc, form, one_uri_line,
                        one_request_param_line, one_response_param_line
) {

    if (testLog != null) {

        // 设定method
        $("#method").find("option[value=" + doc['method'] + "]").attr("selected", true);

        // 设定api地址
        $('#url').val(doc['url']);

        // 设定api名称
        $('#apiName').val(doc['name']);

        if (testLog['uriParameters'] != null) {
            initUriFieldDoc(testLog['uriParameters'], one_uri_line);
        }

        if (testLog['requestHeaderParameters'] != null) {
            initRequestHeaderFieldDoc(testLog['requestHeaderParameters']);
        }

        if (testLog['requestBodyParameters'] != null) {
            // initRequestParamDoc(testLog['requestBodyParameters'], one_request_param_line);
            initRequestBody(testLog['requestBodyParameters']);
        }

        if (testLog['responseBodyParameters'] != null && testLog['responseBodyParameters'].length > 0) {
            initResponseParamDoc(testLog['responseBodyParameters'], one_response_param_line);
        }
        form.render()
    }
}

function initRequestBody(requestBodyParameters) {
    var jsonText = formatJson(projectToJson(JSON.stringify(requestBodyParameters)));
    var xmlText = formatXml(projectToXml(JSON.stringify(requestBodyParameters)));

    $("#request_body_json_text").val(jsonText);
    $("#request_body_xml_text").val(xmlText);

    $("#body-fieldset").addClass("layui-show");
}

function initResponseParamDoc(responseFields, one_response_param_line) {
    for (let i = 0; i < responseFields.length; i++) {
        $("#response-fieldset").append(one_response_param_line);
    }
    var all_input_line = $("#response-fieldset").children(".one-response-body-line");
    for (let i = 0; i < all_input_line.length; i++) {
        let line = all_input_line[i];

        $(line).find("input").each(function () {
            if (this.name === 'responseFieldPath') {
                this.value = responseFields[i]['path']
            }
        });

        $($(line).find("select:first-child"))
            .find("option[value=" + responseFields[i]['type'] + "]")
            .attr("selected", true);
    }

    if (responseFields.length > 0) {
        $("#response-fieldset").addClass("layui-show");
    }
}

function initRequestParamDoc(requestFields, one_request_param_line) {
    for (let i = 0; i < requestFields.length; i++) {
        $("#body-fieldset").append(one_request_param_line);
    }

    var all_input_line = $("#body-fieldset").children(".one-line");

    for (let i = 0; i < all_input_line.length; i++) {
        let line = all_input_line[i];
        $(line).find("input").each(function () {
            if (this.name === 'requestFieldPath') {
                this.value = requestFields[i]['path']
            } else if (this.name === 'requestFieldValue') {
                this.value = requestFields[i]['value']
            }
        });
        $($(line).find("select:first-child"))
            .find("option[value=" + requestFields[i]['type'] + "]")
            .attr("selected", true);
    }

    if (requestFields.length > 0) {
        $("#body-fieldset").addClass("layui-show");
    }
}


function initRequestHeaderFieldDoc(headerFields) {
    var keys = Object.keys(headerFields);
    for (let i = 0; i < keys.length; i++) {
        $("#header-fieldset").append(getNewHeaderLine());
    }
    var all_input_line = $("#header-fieldset").children(".one-request-header-line");
    for (let i = 0; i < all_input_line.length; i++) {
        let line = all_input_line[i];
        $(line).find("input")
            .each(function () {
                if (this.name === 'headerKey') {
                    this.value = keys[i];
                } else if (this.name === 'headerValue') {
                    this.value = headerFields[keys[i]];
                }
            });
    }
    if (keys.length > 0) {
        $("#header-fieldset").addClass("layui-show");
    }

}

function initUriFieldDoc(uriVariables, oneuriline) {
    var keys = Object.keys(uriVariables);
    for (let i = 0; i < keys.length; i++) {
        $("#uri-fieldset").append(oneuriline);
    }
    var all_input_line = $("#uri-fieldset").children(".one-uri-header-line");

    for (let i = 0; i < all_input_line.length; i++) {
        let line = all_input_line[i];
        $(line).find("input")
            .each(function () {
                if (this.name === 'uriField') {
                    this.value = keys[i];
                } else if (this.name === 'uriValue') {
                    this.value = uriVariables[keys[i]];
                }
            });
    }

    if (keys.length > 0) {
        $("#uri-fieldset").addClass("layui-show");
    }
}