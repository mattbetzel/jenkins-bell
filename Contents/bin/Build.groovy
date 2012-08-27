
/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 27/08/2012
 * Time: 06:50
 * To change this template use File | Settings | File Templates.
 */
class Build {

    String name
    String job
    String server
    Throwable fetchError
    String buildState
    String lastBuildState
    boolean building
    Date date
    List<String> authors
    List<String> changes

    def fetch() {
        def url = new URL("http://${server}/job/${job}/lastBuild/api/json")
        def jsonText
        try {
            jsonText = url.text
            fetchError = null
        } catch (Exception e) {
            fetchError = e
            return false
        }

        def json = new groovy.json.JsonSlurper().parseText(jsonText)

        def result = json.result

        building = json.building
        // fixing state if build drops state to null
        if(!building){
            lastBuildState = buildState
            buildState = result
        }


        date = new Date(json.timestamp)
        authors = json.culprits?.fullName.collect {it.toString()}
        changes = json.changeSet?.items?.collect {it.msg.trim().split("\n")}.flatten()
        return true
    }

    def boolean isStateChanged(){
        buildState != lastBuildState
    }

    def boolean isStateSuccess(){
        isSuccess(buildState)
    }

    def boolean isSuccess(String state){
        return state == "SUCCESS"
    }

    def String getLastBuildStateWithColor(){
        isSuccess(lastBuildState) ? TColor.greenFg(lastBuildState) : TColor.redFg(lastBuildState)
    }

    def String getBuildStateWithColor(){
        isSuccess(buildState) ? TColor.greenFg(buildState) : TColor.redFg(buildState)
    }

    def String getStateDescriptionWithColor(){
        building ? TColor.yellowBg("BUILDING ${getBuildStateWithColor()}") : "$lastBuildStateWithColor -> $buildStateWithColor"
    }

    def String getFetchMessageWithColor(){
        fetchError? TColor.redFg(fetchError.toString()) : TColor.greenFg("SUCCESS")
    }

    def String getFileName(){
        name.replaceAll("[^\\w]", "_")
    }

    def URI getBuildUri(){
        new URI("http://$server/job/$job/lastBuild")
    }

//    def File getStateFile(){
//        Files.get().stateFile(fileName)
//    }
//
//    def File getReportFile(){
//        Files.get().reportFile(fileName)
//    }

}