package sample.dyn.repo

import org.testcontainers.containers.GenericContainer

//Per https://github.com/testcontainers/testcontainers-java/issues/318
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)