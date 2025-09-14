class Repo { fun save() {} }

class Service {
    private val repo: Repo = Repo()
    fun helper() {}
    fun a() { repo.save(); helper() }
}

class Client {
    private val svc: Service = Service()
    fun call() { svc.a(); svc.helper() }
}
