<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>测试跨域</title>
    <script src="https://cdn.bootcss.com/jquery/3.3.1/jquery.js"></script>
    <script>
        $(document).ready(function () {
            $("#btn").click(function () {
//                var htmlObj = $.ajax({url: 'https://hacker-news.firebaseio.com/v0/jobstories.json', async: false});
                $.ajax({
//                    url: 'https://api.github.com/users/uptonking',
                    url: 'http://localhost:8999/user/sample1',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        console.log('========success')
                        console.log(data)
                        $("#myDiv").html(data.toString());

                    },
                    error: function (data) {
                        console.log('========error')
                        console.log(data)
                    }

                });

            });
        });
    </script>
</head>
<body>
<button id="btn" type="button">Send Request</button>

<div id="myDiv" style="background-color: #fcfcfc;margin-left: 10%;width: 80%">
    <h2>response placeholder: </h2>
    <h2>待请求内容的占位符</h2>
</div>
</body>
</html>
