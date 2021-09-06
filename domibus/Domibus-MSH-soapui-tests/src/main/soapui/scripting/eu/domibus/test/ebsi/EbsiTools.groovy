package eu.domibus.test.ebsi

class EbsiTools {
    def context = null;
    def log = null;

    static def DEFAULT_ADMIN_USER="admin";
    static def DEFAULT_ADMIN_USER_PWD="123456";
    static def XSFRTOKEN_C2=null;
    static def XSFRTOKEN_C3=null;

    EbsiTools(log, context) {
        this.log = log
        this.context = context
    }

    def getMessageSignatureXML(String side, String messageId, String authenticationUser = DEFAULT_ADMIN_USER, authenticationPwd = DEFAULT_ADMIN_USER_PWD){


        this.log.info "  getMessageSignatureXML  for messageId  \"" + messageId + "\"."

        def commandString = ["curl", urlToDomibus(side) + "/rest/timestamp/download?messageId=" + messageId,
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, authenticationUser, authenticationPwd),
                             "GET",
                             "-v"]
        def result = runCommandInShell(commandString)
        assert(result[0].contains("Envelope")), "Invalid SOAP envelope for message ["+messageId+"].";
        return result[0];
    }

    def getReferenceMessageSignatureXML(String side, String messageId, String authenticationUser = DEFAULT_ADMIN_USER, authenticationPwd = DEFAULT_ADMIN_USER_PWD){


        this.log.info "  getReferenceMessageSignatureXML  for messageId  \"" + messageId + "\"."

        def commandString = ["curl", urlToDomibus(side) + "/rest/timestamp/downloadByRefMessageId?messageId=" + messageId,
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, authenticationUser, authenticationPwd),
                             "GET",
                             "-v"]
        def result = runCommandInShell(commandString)
        assert(result[0].contains("Envelope")), "Invalid SOAP envelope for message ["+messageId+"].";
        return result[0];
    }

    def fetchCookieHeader(String side, String userLogin = DEFAULT_ADMIN_USER, passwordLogin = DEFAULT_ADMIN_USER_PWD) {
        def commandString;
        def commandResult;

        def json = ifWindowsEscapeJsonString('{\"username\":\"' + "${userLogin}" + '\",\"password\":\"' + "${passwordLogin}" + '\"}')

        commandString = ["curl", urlToDomibus(side) + "/rest/security/authentication",
                         "-i",
                         "-H",  "Content-Type: application/json",
                         "--data-binary", json, "-c", this.context.expand('${projectDir}') + File.separator + "cookie.txt",
                         "--trace-ascii", "-"]

        commandResult = runCommandInShell(commandString)
        assert(commandResult[0].contains("XSRF-TOKEN")),"Error:Authenticating user: Error while trying to connect to domibus."
        return commandResult[0];
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    String returnXsfrToken(String side, String userLogin = DEFAULT_ADMIN_USER, passwordLogin = DEFAULT_ADMIN_USER_PWD) {

        String output;
        switch (side.toLowerCase()) {
            case "c2":
            case "blue":
            case "sender":
            case "c2default":
                if (XSFRTOKEN_C2 == null) {
                    output = fetchCookieHeader(side, userLogin, passwordLogin)
                    XSFRTOKEN_C2 = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
                }
                return XSFRTOKEN_C2;
                break;
            case "c3":
            case "red":
            case "receiver":
            case "c3default":
                if (XSFRTOKEN_C3 == null) {
                    output = fetchCookieHeader(side, userLogin, passwordLogin)
                    XSFRTOKEN_C3 = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
                }
                return XSFRTOKEN_C3;
                break;
            default:
                assert(false), "returnXsfrToken: Unknown side. Supported values: sender, receiver, receivergreen ...";
        }
        assert(false), "returnXsfrToken: Error while retrieving XSFRTOKEN ..."
    }


    //---------------------------------------------------------------------------------------------------------------------------------
// Return url to specific domibus
    String urlToDomibus(side) {
        // Return url to specific domibus base on the "side"
        def propName = ""
        switch (side.toLowerCase()) {
            case "c2":
            case "blue":
            case "sender":
            case "c2default":
                propName = "localUrl"
                break;
            case "c3":
            case "red":
            case "receiver":
            case "c3default":
                propName = "remoteUrl"
                break;
            default:
                assert(false), "Unknown side. Supported values: sender, receiver,"
        }
        return this.context.expand("\${#Project#${propName}}")
    }

    static def ifWindowsEscapeJsonString(json) {
        if (System.properties['os.name'].toLowerCase().contains('windows'))
            json = json.replace("\"", "\\\"")
        return json
    }

    // Run curl command
    def runCommandInShell(inputCommand) {
        def proc;
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        this.log.info "  runCommandInShell   [][]  Run curl command: " + inputCommand
        if (inputCommand) {
            proc = inputCommand.execute()
            if (proc != null) {
                //proc.consumeProcessOutput(outputCatcher, errorCatcher);
                proc.waitForProcessOutput(outputCatcher, errorCatcher);
            }
        }
        return ([outputCatcher.toString(), errorCatcher.toString()]);
    }
}
