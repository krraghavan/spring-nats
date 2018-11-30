package com.github.krr.nats.tests;

import com.github.krr.nats.annotations.RuntimeDependency;
import com.github.krr.nats.config.ClassExistsCondition;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@ContextConfiguration
public class ClassExistsConditionTest extends AbstractTestNGSpringContextTests {

  @Test
  public void mustMatchWhenAllRuntimeDependenciesAreAvailable() {
    assertNotNull(applicationContext, "ApplicationContext cannot be null");
    Object bean = applicationContext.getBean(ClassWithValidRuntimeDependency.class);
    assertNotNull(bean);
  }

  @Test(expectedExceptions = NoSuchBeanDefinitionException.class)
  public void mustNotMatchWhenRuntimeDependencyIsNotAvailable() {
    assertNotNull(applicationContext, "ApplicationContext cannot be null");
    applicationContext.getBean(ClassWithoutValidRuntimeDependency.class);
  }

  @Configuration
  static class ConfigurationForConditionTest{

    @Bean
    @Conditional(ClassExistsCondition.class)
    @RuntimeDependency(classes = {
        "java.lang.String",
        "java.lang.Object"
    })
    public ClassWithValidRuntimeDependency classWithAllValidRuntimeDependencies() {
      return new ClassWithValidRuntimeDependency();
    }

    @Bean
    @Conditional(ClassExistsCondition.class)
    @RuntimeDependency(classes = {
        "java.lang.String",
        "a.b.c.d.XYZ",
    })
    public ClassWithoutValidRuntimeDependency classWithoutAllValidRuntimeDependencies() {
      return new ClassWithoutValidRuntimeDependency();
    }

    /*
     * This bean was used to test that the config load would fail if the {@link RuntimeDependency}
     * annotation is missing.  This would cause the test startup itself to fail so we comment it
     * out.
     */
//    @Bean
//    @Conditional(ClassExistsCondition.class)
//    public ClassWithoutRuntimeDependencyAnnotation classWithoutRuntimeDependencyAnnotation() {
//      return new ClassWithoutRuntimeDependencyAnnotation();
//    }

  }

  private static class ClassWithoutRuntimeDependencyAnnotation {
  }

  private static class ClassWithValidRuntimeDependency {

  }

  private static class ClassWithoutValidRuntimeDependency {

  }

}