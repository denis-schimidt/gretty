/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class FarmAfterIntegrationTestTask extends FarmStopTask {

  String integrationTestTask

  protected Map webAppRefs = [:]

  String getEffectiveIntegrationTestTask() {
    integrationTestTask ?: new FarmConfigurer(project).getProjectFarm(farmName).integrationTestTask
  }

  Iterable<WebAppConfig> getWebAppConfigsForProjects() {
    FarmConfigurer configurer = new FarmConfigurer(project)
    Map wrefs = [:]
    FarmConfigurer.mergeWebAppRefMaps(wrefs, webAppRefs)
    FarmConfigurer.mergeWebAppRefMaps(wrefs, configurer.getProjectFarm(farmName).webAppRefs)
    if(!wrefs)
      wrefs = configurer.getDefaultWebAppRefMap()
    configurer.getWebAppConfigsForProjects(wrefs)
  }

  void setupIntegrationTestTaskDependencies() {
    def thisTask = this
    println "DBG ${thisTask.name}: effectiveIntegrationTestTask=${thisTask.effectiveIntegrationTestTask}"
    println "DBG ${thisTask.name}: webAppProjects=${getWebAppConfigsForProjects().collect { project.project(it.projectPath) }}"
    getWebAppConfigsForProjects().each {
      project.project(it.projectPath).tasks.all { t ->
        if(t.name == thisTask.effectiveIntegrationTestTask)
          thisTask.mustRunAfter t
        else if(t instanceof JettyAfterIntegrationTestTask)
          thisTask.mustRunAfter t
      }
    }
  }

  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs[w] = options
  }
}
