package com.example.springbatches.config;

import com.example.springbatches.entity.Customer;
import com.example.springbatches.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {
/*
   Jobs Have ----->Steps--->ItemReader ,ItemProcessor,ItemWriter
   Item Reader reads from csv file
   Item Processor process the records depends on logic
   Item Writer writes the records to Database
*/
    private JobBuilderFactory jobBuilderFactory; //To Create Job
    private StepBuilderFactory stepBuilderFactory; //To Create Step
    private CustomerRepository customerRepository; // To save the data inject

    // FlatFileItemReader reades the file from csv Which is customer type

    @Bean // Manullay user has to create a bean object when the class is @confirguration
     public FlatFileItemReader<Customer>  reader()
     {
         FlatFileItemReader<Customer> itemReader=new FlatFileItemReader<>();
         itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
         itemReader.setName("csvReader");
         itemReader.setLinesToSkip(1);
         itemReader.setLineMapper(lineMapper());
         return itemReader;
     }
     private LineMapper<Customer> lineMapper()
     {
         DefaultLineMapper<Customer> lineMapper=new DefaultLineMapper<>();

         //DelimitedLineTokenizer is used to seprate the csv file using comma, and also set the names
         DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
         lineTokenizer.setDelimiter(",");
         lineTokenizer.setStrict(false);
         lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob");

         // BeanWrapperFoeldSetMappper maps this csv fields to Customer Object

         BeanWrapperFieldSetMapper<Customer> fieldSetMapper=new BeanWrapperFieldSetMapper<>();
         fieldSetMapper.setTargetType(Customer.class);

         lineMapper.setLineTokenizer(lineTokenizer);
         lineMapper.setFieldSetMapper(fieldSetMapper);
         return lineMapper;
     }


     //After reading next is process the data
     @Bean
     public  CustomerProcessor processor()
     {
         return new CustomerProcessor();
     }


    //After processing write the data to customRepository

     @Bean
     public RepositoryItemWriter<Customer> writer()
     {
      RepositoryItemWriter<Customer> writer=new RepositoryItemWriter<>();
      writer.setRepository(customerRepository);
      writer.setMethodName("save");
      return writer;
     }

     // Create the step object  and give reader,process and writer object to step
    //after creating the step we have to give to job
    @Bean
     public Step step1()
     {
         // step name is csv-step
         return stepBuilderFactory.get("csv-step").<Customer,Customer>chunk(10)
                 .reader(reader())
                 .processor(processor())
                 .writer(writer())
                 .build();
     }

     //Job is an Interface we have to give step to job
     @Bean
     public Job runJob()
     {
         return jobBuilderFactory.get("importCustomers")//Job name is import customers
                 .flow(step1()).end().build(); //step 1 gives to job
     }


     //Task Executor is for executing the tasks parallely
     @Bean
     public TaskExecutor taskExecutor()
     {
         SimpleAsyncTaskExecutor asyncTaskExecutor=new SimpleAsyncTaskExecutor();
         asyncTaskExecutor.setConcurrencyLimit(10);
         return  asyncTaskExecutor;
     }



}
