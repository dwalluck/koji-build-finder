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

import static j2html.TagCreator.attrs;
import static j2html.TagCreator.caption;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.nio.file.Path;
import java.util.Collection;

import org.jboss.pnc.build.finder.core.BuildStatistics;
import org.jboss.pnc.build.finder.koji.KojiBuild;

import j2html.tags.ContainerTag;
import j2html.tags.specialized.TableTag;

public final class BuildStatisticsReport extends Report {
    private static final String NAME = "Statistics";

    private static final String DESCRIPTION = "Number of builds/artifacts built from source";

    private static final String BASE_FILENAME = "statistics";

    private static final double ONE_HUNDRED_PERCENT = 100.00D;

    private final BuildStatistics buildStatistics;

    public BuildStatisticsReport(Path outputDirectory, Collection<KojiBuild> builds) {
        this(outputDirectory, BASE_FILENAME, builds);
    }

    public BuildStatisticsReport(Path outputDirectory, String baseFilename, Collection<KojiBuild> builds) {
        super(NAME, DESCRIPTION, baseFilename, outputDirectory);
        buildStatistics = new BuildStatistics(builds);
    }

    public BuildStatistics getBuildStatistics() {
        return buildStatistics;
    }

    @Override
    public ContainerTag<TableTag> toHTML() {
        return table(
                attrs("#table-" + getBaseFilename()),
                caption(text(getName())),
                thead(
                        tr(
                                th(),
                                th(text("Built")),
                                th(text("Imported")),
                                th(text("Total")),
                                th(text("Percent built from source")),
                                th(text("Percent imported")))),
                tbody(
                        tr(
                                td(strong(text("Builds"))),
                                td(
                                        text(
                                                String.valueOf(
                                                        buildStatistics.getNumberOfBuilds()
                                                                - buildStatistics.getNumberOfImportedBuilds()))),
                                td(text(String.valueOf(buildStatistics.getNumberOfImportedBuilds()))),
                                td(text(String.valueOf(buildStatistics.getNumberOfBuilds()))),
                                td(text((ONE_HUNDRED_PERCENT - buildStatistics.getPercentOfBuildsImported()) + "%")),
                                td(text(buildStatistics.getPercentOfBuildsImported() + "%"))),
                        tr(
                                td(strong(text("Archives"))),
                                td(
                                        text(
                                                String.valueOf(
                                                        buildStatistics.getNumberOfArchives()
                                                                - buildStatistics.getNumberOfImportedArchives()))),
                                td(text(String.valueOf(buildStatistics.getNumberOfImportedArchives()))),
                                td(text(String.valueOf(buildStatistics.getNumberOfArchives()))),
                                td(text((ONE_HUNDRED_PERCENT - buildStatistics.getPercentOfArchivesImported()) + "%")),
                                td(text(buildStatistics.getPercentOfArchivesImported() + "%")))));
    }
}
