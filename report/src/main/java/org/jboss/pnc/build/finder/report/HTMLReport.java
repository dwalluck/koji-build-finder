/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.build.finder.report;

import static j2html.TagCreator.a;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.body;
import static j2html.TagCreator.caption;
import static j2html.TagCreator.div;
import static j2html.TagCreator.document;
import static j2html.TagCreator.each;
import static j2html.TagCreator.footer;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.main;
import static j2html.TagCreator.ol;
import static j2html.TagCreator.span;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;
import static java.lang.String.join;
import static java.lang.String.valueOf;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jboss.pnc.build.finder.core.Utils;
import org.jboss.pnc.build.finder.koji.KojiBuild;
import org.jboss.pnc.build.finder.koji.KojiLocalArchive;

import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiRpmInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;

import j2html.attributes.Attr;
import j2html.tags.ContainerTag;
import j2html.tags.Tag;

public final class HTMLReport extends Report {
    private static final String NAME = "Build Finder";

    private static final String GITHUB_URL = "https://github.com/project-ncl/build-finder/";

    private static final String HTML_STYLE = "body{font-family:Verdana,Helvetica,Arial,sans-serif;font-size:13px}"
            + "table{width:100%;border-style:solid;border-width:1px;border-collapse:collapse}"
            + "caption{background:#FFFACD;caption-side:top;font-weight:700;font-size:larger;text-align:left;"
            + "margin-top:50px}"
            + "th{border-style:solid;border-width:1px;background-color:#A9A9A9;text-align:left;font-weight:700}"
            + "tr{border-style:solid;border-width:1px}tr:nth-child(even){background-color:#D3D3D3}"
            + "td{border-style:solid;border-width:1px;text-align:left;vertical-align:top;font-size:small}"
            + "footer{font-size:smaller}";

    private final URL kojiwebUrl;

    private final URL pncUrl;

    private final List<KojiBuild> builds;

    private final List<Report> reports;

    public HTMLReport(
            File outputDirectory,
            Iterable<String> files,
            List<KojiBuild> builds,
            URL kojiwebUrl,
            URL pncUrl,
            List<Report> reports) {
        setName("Build Report for " + join(", ", files));
        setDescription("List of analyzed artifacts whether or not they were found in a Koji build");
        setBaseFilename("output");
        setOutputDirectory(outputDirectory);

        this.builds = builds;
        this.kojiwebUrl = kojiwebUrl;
        this.pncUrl = pncUrl;
        this.reports = reports;
    }

    private static ContainerTag errorText(String text) {
        return span(text).withStyle("color:red;font-weight:700");
    }

    private Tag<?> linkBuild(KojiBuild build) {
        int id = build.getId();

        if (build.isPnc()) {
            return a()
                    .withHref(pncUrl + "/pnc-web/#/builds/" + build.getBuildInfo().getExtra().get("external_build_id"))
                    .with(text(Integer.toString(id)));
        }

        return a().withHref(kojiwebUrl + "/buildinfo?buildID=" + id).with(text(Integer.toString(id)));
    }

    private Tag<?> linkPkg(KojiBuild build) {
        String name = build.getBuildInfo().getName();

        if (build.isPnc()) {
            return a()
                    .withHref(
                            pncUrl + "/pnc-web/#/projects/" + build.getBuildInfo().getExtra().get("external_project_id")
                                    + "/build-configs/"
                                    + build.getBuildInfo().getExtra().get("external_build_configuration_id"))
                    .with(text(name));
        }

        int id = build.getBuildInfo().getPackageId();
        return a().withHref(kojiwebUrl + "/packageinfo?packageID=" + id).with(text(name));
    }

    private Tag<?> linkArchive(KojiBuild build, KojiArchiveInfo archive, Collection<String> unmatchedFilenames) {
        String name = archive.getFilename();
        Integer id = archive.getArchiveId();
        boolean validId = id != null && id > 0;

        if (!unmatchedFilenames.isEmpty()) {
            String archives = join(", ", unmatchedFilenames);

            name += " (unmatched files: " + archives + ")";
        }

        boolean error = !unmatchedFilenames.isEmpty() || build.isImport() || !validId;

        if (build.isPnc()) {
            return error ? errorText(name)
                    : a().withHref(pncUrl + "/pnc-web/#/artifacts/" + archive.getArchiveId()).with(text(name));
        }

        String href = "/archiveinfo?archiveID=" + id;

        if (error) {
            if (validId) {
                return a().withHref(kojiwebUrl + href).with(errorText(name));
            }

            return errorText(name);
        }

        return a().withHref(kojiwebUrl + href).with(text(name));
    }

    private Tag<?> linkArchive(KojiBuild build, KojiArchiveInfo archive) {
        return linkArchive(build, archive, Collections.emptyList());
    }

    private Tag<?> linkRpm(KojiBuild build, KojiRpmInfo rpm) {
        String name = rpm.getName() + "-" + rpm.getVersion() + "-" + rpm.getRelease() + "." + rpm.getArch() + ".rpm";
        Integer id = rpm.getId();
        String href = "/rpminfo?rpmID=" + id;
        boolean error = build.isImport() || id <= 0;
        return error ? a().withHref(kojiwebUrl + href).with(errorText(name))
                : a().withHref(kojiwebUrl + href).with(text(name));
    }

    private Tag<?> linkLocAr(KojiBuild build, KojiLocalArchive localArchive) {
        KojiArchiveInfo archive = localArchive.getArchive();
        KojiRpmInfo rpm = localArchive.getRpm();
        Collection<String> unmatchedFilenames = localArchive.getUnmatchedFilenames();

        if (rpm != null) {
            return linkRpm(build, rpm);
        } else if (archive != null) {
            return linkArchive(build, archive, unmatchedFilenames);
        } else {
            return errorText("Error linking local archive with files: " + localArchive.getFilenames());
        }
    }

    private Tag<?> linkTag(KojiBuild build, KojiTagInfo tag) {
        int id = tag.getId();
        String name = tag.getName();

        if (build.isPnc()) {
            return li(
                    a().withHref(
                            pncUrl + "/pnc-web/#/product/" + build.getBuildInfo().getExtra().get("external_product_id")
                                    + "/version/" + build.getBuildInfo().getExtra().get("external_version_id"))
                            .with(text(name)));
        }

        return li(a().withHref(kojiwebUrl + "/taginfo?tagID=" + id).with(text(name)));
    }

    private static Tag<?> linkSource(KojiBuild build) {
        String source = build.getSource().orElse("");
        return span(source);
    }

    @Override
    public ContainerTag toHTML() {
        return html(
                head(style().withText(HTML_STYLE)).with(title().withText(getName())),
                body().with(
                        header(h1(getName())),
                        main(
                                div(
                                        attrs("#div-reports"),
                                        table(
                                                caption(text("Reports")),
                                                thead(tr(th(text("Name")), th(text("Description")))),
                                                tbody(
                                                        tr(
                                                                td(
                                                                        a().withHref("#div-" + getBaseFilename())
                                                                                .with(text("Builds"))),
                                                                td(text(getDescription()))),
                                                        each(
                                                                reports,
                                                                report -> tr(
                                                                        td(
                                                                                a().withHref(
                                                                                        "#div-" + report
                                                                                                .getBaseFilename())
                                                                                        .with(text(report.getName()))),
                                                                        td(text(report.getDescription()))))))),
                                div(
                                        attrs("#div-" + getBaseFilename()),
                                        table(
                                                caption(text("Builds")),
                                                thead(
                                                        tr(
                                                                th(text("#")),
                                                                th(text("ID")),
                                                                th(text("Name")),
                                                                th(text("Version")),
                                                                th(text("Artifacts")),
                                                                th(text("Tags")),
                                                                th(text("Type")),
                                                                th(text("Sources")),
                                                                th(text("Patches")),
                                                                th(text("SCM URL")),
                                                                th(text("Options")),
                                                                th(text("Extra")))),
                                                tbody(
                                                        each(
                                                                builds,
                                                                b -> tr(
                                                                        td(text(Integer.toString(builds.indexOf(b)))),
                                                                        td(
                                                                                b.getId() > 0 ? linkBuild(b)
                                                                                        : errorText(
                                                                                                valueOf(b.getId()))),
                                                                        td(b.getId() > 0 ? linkPkg(b) : text("")),
                                                                        td(
                                                                                b.getId() > 0 ? text(
                                                                                        b.getBuildInfo()
                                                                                                .getVersion()
                                                                                                .replace('_', '-'))
                                                                                        : text("")),
                                                                        td(
                                                                                b.getArchives() != null ? ol(
                                                                                        each(
                                                                                                b.getArchives(),
                                                                                                a -> li(
                                                                                                        linkLocAr(b, a),
                                                                                                        text(": "),
                                                                                                        text(
                                                                                                                join(
                                                                                                                        ", ",
                                                                                                                        a.getFilenames())))))
                                                                                        : text("")),
                                                                        td(
                                                                                b.getTags() != null ? ul(
                                                                                        each(
                                                                                                b.getTags(),
                                                                                                tag -> linkTag(b, tag)))
                                                                                        : text("")),
                                                                        td(
                                                                                b.getMethod().isPresent()
                                                                                        ? text(b.getMethod().get())
                                                                                        : b.getId() > 0 ? errorText(
                                                                                                "imported build")
                                                                                                : text("")),
                                                                        td(
                                                                                b.getScmSourcesZip().isPresent()
                                                                                        ? linkArchive(
                                                                                                b,
                                                                                                b.getScmSourcesZip()
                                                                                                        .get())
                                                                                        : text("")),
                                                                        td(
                                                                                b.getPatchesZip().isPresent()
                                                                                        ? linkArchive(
                                                                                                b,
                                                                                                b.getPatchesZip().get())
                                                                                        : text("")),
                                                                        td(
                                                                                b.getSource().isPresent()
                                                                                        ? linkSource(b)
                                                                                        : b.getId() == 0 ? text("")
                                                                                                : errorText(
                                                                                                        "missing URL")),
                                                                        td(
                                                                                b.getTaskInfo() != null
                                                                                        && b.getTaskInfo()
                                                                                                .getMethod() != null
                                                                                        && "maven".equals(
                                                                                                b.getTaskInfo()
                                                                                                        .getMethod())
                                                                                        && b.getTaskRequest() != null
                                                                                        && b.getTaskRequest()
                                                                                                .asMavenBuildRequest()
                                                                                                .getProperties() != null
                                                                                        && b.getTaskRequest()
                                                                                                .asMavenBuildRequest() != null
                                                                                                        ? each(
                                                                                                                b.getTaskRequest()
                                                                                                                        .asMavenBuildRequest()
                                                                                                                        .getProperties()
                                                                                                                        .entrySet(),
                                                                                                                entry -> text(
                                                                                                                        entry.getKey()
                                                                                                                                + (entry.getValue() != null
                                                                                                                                        ? "=" + entry
                                                                                                                                                .getValue()
                                                                                                                                                + "; "
                                                                                                                                        : "; ")))
                                                                                                        : text("")),
                                                                        td(
                                                                                b.getBuildInfo().getExtra() != null
                                                                                        ? each(
                                                                                                b.getBuildInfo()
                                                                                                        .getExtra()
                                                                                                        .entrySet(),
                                                                                                entry -> text(
                                                                                                        entry.getKey()
                                                                                                                + (entry.getValue() != null
                                                                                                                        ? "=" + entry
                                                                                                                                .getValue()
                                                                                                                                + "; "
                                                                                                                        : "; ")))
                                                                                        : text(""))))))),
                                each(
                                        reports,
                                        report -> div(attrs("#div-" + report.getBaseFilename()), report.toHTML()))),
                        div(
                                attrs("#div-footer"),
                                footer().attr(Attr.CLASS, "footer")
                                        .attr(Attr.ID, "footer")
                                        .with(
                                                text("Created: " + LocalDateTime.now() + " by "),
                                                a().withHref(GITHUB_URL).with(text(NAME)),
                                                text(" " + Utils.getBuildFinderVersion() + " (SHA: "),
                                                a().withHref(
                                                        GITHUB_URL + "/commit/" + Utils.getBuildFinderScmRevision())
                                                        .with(text(Utils.getBuildFinderScmRevision() + ")"))))));
    }

    @Override
    public Optional<String> renderText() {
        return Optional.of(document().render() + toHTML().render());
    }
}
