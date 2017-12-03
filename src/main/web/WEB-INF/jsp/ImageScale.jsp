<%@page contentType="text/html;charset=UTF-8" language="java"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="s"%>



<html>
    <head>
        <title>Image Scaler Service</title>
        <link rel="stylesheet" href="<c:url value="uikit/css/uikit.min.css"/>" />
        <script src="<c:url value="jquery/jquery.min.js"/>"></script>
        <script src="<c:url value="uikit/js/uikit.min.js"/>"></script>
        <script src="<c:url value="uikit/js/uikit-icons.min.js"/>"></script>
    </head>
    <script>
        var fileSizes = new Array(4);

        function calcSize() {
            $("input").each(function(index) {
                $(this).change();
            });
        }

        function setSummarySize() {
            var size = 0;
            for (i=0; i<fileSizes.length; i++) {
                size += fileSizes[i];
            }
            if (size > 1024) {
                $("#sizeSummary").html("Gesamtgr&ouml;&szlig;e " + Math.ceil(size / 1024) + " MB");
            } else {
                $("#sizeSummary").html("Gesamtgr&ouml;&szlig;e " + size + " kB");
            }
        }
    </script>
    <body onload="calcSize()">
        <div><s:errors field="attachment"/></div>
        <div class="file-upload uk-card uk-card-default uk-card-body uk-width-1-2@m">
            <h1>Bilderservice&nbsp;<span uk-icon="icon: cloud-upload; ratio: 3"/></h1>
            <s:form beanclass="org.datenmuehle.stripes.action.ImageScaleAction" id="uploads">
                <fieldset class="uk-fieldset">
                    <div class="uk-margin">
                        <span class="uk-text-middle">Ereignis / Veranstaltung</span>
                    </div>

                    <input class="uk-input" name="event" type="text" placeholder="Ereignis">

                    <div class="uk-margin">
                        <span class="uk-text-middle">Horizontale Aufl&ouml;sung</span>
                    </div>

                    <div class="uk-margin">
                        <s:select name="selectedHRes" class="uk-select">
                            <s:option value="">w&auml;hlen...</s:option>
                            <s:options-collection collection="${actionBean.hres}"/>
                        </s:select>
                    </div>

                    <div class="uk-margin">
                        <span class="uk-text-middle">Dateien ausw&auml;hlen</span>
                     </div>

                    <div>
                        <c:forEach var="index" begin="0" end="3">
                            <div class="uk-margin" uk-margin>
                                <div uk-form-custom="target: true">
                                    <s:file name="attachments[${index}]" id="file${index}"/>
                                    <input class="uk-input uk-form-width-medium" type="text" placeholder="..." disabled/>
                                    <script>
                                        $('#file${index}').bind('change', function() {
                                            fileSizes[${index}] = 0;
                                            if ($("#file${index}")[0].files[0]) {
                                                var fileSize = Math.ceil($("#file${index}")[0].files[0].size / 1024);
                                                $("label[for='file${index}']").text(fileSize + " kb");

                                                fileSizes[${index}] = Number(fileSize);
                                                setSummarySize();
                                            }
                                        });
                                    </script>
                                    &nbsp;<label for="file${index}" id="label${index}"></label>
                                </div>
                                <!--<button class="uk-button uk-button-default">Ausw&auml;hlen</button>-->
                            </div>
                        </c:forEach>
                        <span id="sizeSummary">...</span>
                    </div>
                    <div class="uk-margin">
                        <span class="uk-text-middle">An <span class="uk-text-bold">DEINE SEITE</span> &uuml;bertragen</span>
                    </div>
                    <div class="uk-form-custom uk-offcanvas-content" uk-form-custom="">
                        <s:submit name="upload" value="Ok" class="uk-button uk-button-default"/>

                        <!-- The whole page content goes here -->
                        <button class="uk-button uk-button-default uk-margin-small-right" type="button" uk-toggle="target: #offcanvas-usage">Hilfe</button>

                        <div id="offcanvas-usage" uk-offcanvas>
                            <div class="uk-offcanvas-bar">
                                <button class="uk-offcanvas-close" type="button" uk-close></button>
                                <h3>Hilfe</h3>
                                <p>
                                    Mit Hilfe des Bilderservice laden Sie Bilder in einen geschützten Bereich
                                    der Seite DEINE SEITE.
                                    Dabei werden die Bilder auf die angegebene Horizontale Auflösung skaliert. Das bedeutet das
                                    Bilder mit einer gr&ouml;&szlig;eren Aufl&ouml;sung verkleinert werden um Speicherplatz zu sparen.
                                </p>
                                <p>
                                    Die maximale Anzahl gleichzeitig &uuml;bertragbarer Bilder ist auf 4 beschr&auml;nkt.
                                </p>
                                <p>
                                    <b>Wenn Sie eine langsame Internetverbindung haben laden Sie immer nur ein Bild hoch!
                                    Ansonsten kann es zu Verbindungsabbr&uuml;chen kommen.</b>
                                </p>
                            </div>
                        </div>

                    </div>
                </fieldset>
            </s:form>
        </div>
    </body>
</html>