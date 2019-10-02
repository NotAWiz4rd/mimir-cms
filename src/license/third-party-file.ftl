<#--
  #%L
  License Maven Plugin
  %%
  Copyright (C) 2012 Codehaus, Tony Chemit
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.

  You should have received a copy of the GNU General Lesser Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  -->
<#-- To render the third-party file.
 Available context :

 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<#function artifactFormat p licenses>
    <#assign resultLicenses = ""/>
    <#list licenses as license>
        <#assign resultLicenses = resultLicenses + " (" +license + ")"/>
    </#list>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return p.artifactId + ";" + resultLicenses + "; (" + p.groupId + ":" + p.artifactId + ":" + p.version + " - " + (p.url!"no url defined") + ")">
    <#else>
        <#return p.name + ";" + resultLicenses + "; (" + p.groupId + ":" + p.artifactId + ":" + p.version + " - " + (p.url!"no url defined") + ")">
    </#if>
</#function>


Lists of ${dependencyMap?size} third-party dependencies.
<#list dependencyMap as e>
    <#assign project = e.getKey()/>
    <#assign licenses = e.getValue()/>
    ${artifactFormat(project, licenses)}
</#list>
    Google Fonts Heebo; (Open Font License); https://fonts.google.com/specimen/Heebo
    Breeze icon theme, KDE; (GNU LESSER GENERAL PUBLIC LICENSE Version 2.1); https://github.com/KDE/breeze-icons
    Free Video from https://sample-videos.com; FREE
