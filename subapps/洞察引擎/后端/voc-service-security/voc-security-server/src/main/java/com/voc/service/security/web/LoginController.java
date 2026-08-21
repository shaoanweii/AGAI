package com.voc.service.security.web;

//@RestController
//@RequestMapping("/")
//@RequiredArgsConstructor
//@Tag(name = "系统登陆服务")
public class LoginController {
    /*@Autowired
    ILoginServiceClient loginServiceClient;

    @PostMapping(value = "/login")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(
            schema = @Schema(implementation = LoginDemo.class),
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                        name = "账号、口令认证方式",
                        value = "{\n" +
                                "    \"username\":\"admin1\",\n" +
                                "    \"password\":\"Passw0rd@!\",\n" +
                                "    \"appId\":\"insights\",\n" +
                                "    \"type\":\"base\",\n" +
                                "    \"checkKey\":\"123\",\n" +
                                "    \"captcha\":\"2587\"\n" +
                                "}"),
        }))
    @ResponseBody
    Result<AuthenticationResponse> login(@RequestBody LoginDemo login) throws IOException {

        *//*Result<AuthenticationResponse> rs =
        return Result.OK(AuthenticationResponse.builder()
                .userid("1")
                .appId(login.getAppId())
                .username(login.getUsername())
                .type(login.getType())
                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMSIsImlkZW50aXR5X3R5cGUiOiJiYXNlIiwiYXBwX2lkIjoiaW5zaWdodHMiLCJ1c2VybmFtZSI6IkUyYzZ4d0JZVFBEOEg4cUFGTGNHdklvMk1OdkZwaUk0ekZjVnR0akJKaDhrbGxIQzQwdHdHb1pxQ0JMVm9sR0UiLCJzdWIiOiIxIiwiaWF0IjoxNzA5MDE1MTgwLCJleHAiOjE3MTE2MDcxODB9.DVc4esK4GCuQrkwwzZnY8NkrsMH9liM8JV2VZgW6lmg")
//                    .refreshToken(refreshToken)
                .build());*//*
        return  loginServiceClient.login(UserModel.builder()
                        .appId(login.getAppId())
                        .username(login.getUsername())
                        .password(login.getPassword())
                        .checkKey(login.getCheckKey())
                        .captcha(login.getCaptcha())
                        .type(login.getType())
                .build());
    }*/

}
