package org.codeartisans.gradle.wsdl

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class WsdlToJavaTest extends Specification
{
  @Rule
  final TemporaryFolder testProjectDir = new TemporaryFolder() {
    @Override
    void delete()
    {
      // super.delete()
    }
  }
  File buildFile

  def setup()
  {
    buildFile = testProjectDir.newFile( 'build.gradle' )
  }

  def "single ping-pong wsdl"()
  {
    given:
    buildFile << """
      import de.undercouch.gradle.tasks.download.Download
      import org.gradle.plugins.wsdl.*
      
      plugins {
        id "de.undercouch.download" version "3.2.0"
        id "org.gradle.plugins.wsdl-tasks"
      }

      repositories {
        jcenter()
      }
      
      task downloadWsdl(type: Download) {
        src "http://www-inf.int-evry.fr/cours/WebServices/TP_BPEL/files/PingPong.wsdl"
        dest "\$buildDir/wsdls/PingPong.wsdl"
      }

      task wsdl2java(type: WsdlToJava) {
        dependsOn downloadWsdl
        wsdls {
          pingPong {
            wsdl = file(downloadWsdl.dest)
            packageName = 'ping.pong'
          }
        }
      }

    """.stripIndent()

    when:
    def result = GradleRunner.create()
                             .withProjectDir( testProjectDir.root )
                             .withArguments( 'wsdl2java', '-s', '-i' )
                             .withPluginClasspath()
                             .build()

    then:
    println result.output
    result.task( ":wsdl2java" ).outcome == TaskOutcome.SUCCESS

    and:
    new File( testProjectDir.root, "build/generated-sources/wsdl2java/java/ping/pong/PingPongService.java" ).isFile()
  }
}