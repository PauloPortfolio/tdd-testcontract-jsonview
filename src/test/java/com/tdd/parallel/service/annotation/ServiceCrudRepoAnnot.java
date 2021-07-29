package com.tdd.parallel.service.annotation;

import com.tdd.parallel.core.config.ServiceCrudRepoCfg;
import com.tdd.parallel.entity.Person;
import com.tdd.parallel.service.IService;
import com.tdd.testconfig.annotation.CustomTestcontainerConfig;
import com.tdd.testconfig.annotation.CustomTestcontainerConfigClass;
import com.tdd.testconfig.annotation.CustomTestsConfig;
import com.tdd.testconfig.annotation.CustomTestsConfigClass;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.tdd.databuilder.PersonBuilder.personWithIdAndName;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("ServiceCrudRepoAnnot")
@Import(ServiceCrudRepoCfg.class)
@CustomTestsConfig
//@CustomTestcontainerConfig
public class ServiceCrudRepoAnnot {

  final private String enabledTest = "true";
  final private int repet = 1;
  private List<Person> personList;
  private Mono<Person> personMono;

  @Autowired
  private IService serviceCrudRepo;


  @BeforeEach
  public void setUp(TestInfo testInfo) {
    CustomTestsConfigClass.testHeader("STARTING TEST","Method-Name:",
                                      testInfo.getTestMethod()
                                                     .toString()
                                     );
    Person person1 = personWithIdAndName().create();
    personList = Collections.singletonList(person1);
    personMono = Mono.just(person1);
    StepVerifier
         .create(serviceCrudRepo.save(person1)
                                .log())
         .expectNext(person1)
         .verifyComplete();
  }


  @BeforeAll
  public static void beforeAll() {
    CustomTestsConfigClass.beforeAll();
    CustomTestsConfigClass.testHeader("STARTING TEST-CLASS","Name:",
                                      ServiceCrudRepoAnnot.class.getSimpleName()
                                     );
  }


  @AfterAll
  public static void afterAll() {
    CustomTestsConfigClass.afterAll();
    CustomTestsConfigClass.testHeader("ENDING TEST-CLASS","Name:",
                                      ServiceCrudRepoAnnot.class.getSimpleName()
                                     );
  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    StepVerifier
         .create(serviceCrudRepo.deleteAll()
                                .log())
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();

    CustomTestsConfigClass.testHeader("ENDING TEST","Method-Name:",
                                      testInfo.getTestMethod()
                                                     .toString()
                                     );
  }


  //  @Test
  @RepeatedTest(value = repet)
  @DisplayName("SaveAll")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void saveAll() {
    StepVerifier.create(personMono.log())
                .expectNextSequence(personList)
                .verifyComplete();
  }


  //  @Test
  @RepeatedTest(repet)
  @DisplayName("Save")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void save() {
    StepVerifier
         .create(personMono)
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();
  }


  @Test
  //  @RepeatedTest(repet)
  @DisplayName("FindAll")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAll() {
    StepVerifier.create(personMono)
                .expectNextSequence(personList)
                .verifyComplete();

    StepVerifier
         .create(serviceCrudRepo.findAll()
                                .log())
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();
  }


  @Test
  //  @RepeatedTest(repet)
  @DisplayName("FindById")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findById() {
    StepVerifier
         .create(personMono.log())
         .expectSubscription()
         .expectNextMatches(person -> personList.get(0)
                                                .getName()
                                                .equals(person.getName()))
         .verifyComplete();
  }


  @Test
  //  @RepeatedTest(repet)
  @DisplayName("DeleteAll")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void deleteAll() {
    StepVerifier.create(serviceCrudRepo.deleteAll())
                .verifyComplete();

    StepVerifier
         .create(serviceCrudRepo.findAll()
                                .log())
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();
  }


  @Test
  //  @RepeatedTest(repet)
  @DisplayName("DeleteById")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void deleteById() {
    StepVerifier
         .create(serviceCrudRepo.deleteById(personList.get(0)
                                                      .getId()))
         .expectSubscription()
         .verifyComplete();

    Mono<Person> personMono = serviceCrudRepo.findById(personList.get(0)
                                                                 .getId());

    StepVerifier
         .create(personMono)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();
  }


  @Test
  @DisplayName("Container")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void checkContainer() {
    assertTrue(CustomTestcontainerConfigClass.getContainer()
                                             .isRunning());
  }


  @Test
  @DisplayName("BHWorks")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void bHWorks() {
    try {
      FutureTask<?> task = new FutureTask<>(() -> {
        Thread.sleep(0);
        return "";
      });

      Schedulers.parallel()
                .schedule(task);

      task.get(10,TimeUnit.SECONDS);
      Assertions.fail("should fail");
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
    }
  }
}
