podTemplate(cloud: 'k8s-prod',
        label: 'divolte-build-deploy',
        inheritFrom: 'default',
        namespace: 'prod',
        nodeSelector: 'elo7-environment=production',
        containers: [
        containerTemplate(
            name: 'deployer',
            image: 'registry.docker.elo7aws.com.br/deployer:0.0.4',
            ttyEnabled: true,
            alwaysPullImage: true,
            command: 'cat')
        ]
) {
    node("divolte-build-deploy") {
        stage ('helm-package') {
            def helmPackageJob = build job: 'helm-package-v1',
            wait: true,
            propagate: true,
            parameters: [
                string(name: 'HELM_ROOT_PATH', value: "charts/divolte/helm"),
                string(name: 'APP_VERSION', value: "${APP_VERSION}")
            ]
            println helmPackageJob
        }

        stage ('deploy-dev') {
            def helmDeployJob = build job: 'helm-deploy-v1',
            wait: true,
            propagate: true,
            parameters: [
                string(name: 'K8S_CLUSTER', value: "us-east"),
                string(name: 'K8S_NAMESPACE', value: "dev"),
                string(name: 'CHART_NAME', value: "chartmuseum/divolte"),
                string(name: 'HELM_RELEASE_NAME', value: "divolte"),
                string(name: 'GITHUB_FILE_LIST', value: "charts/divolte/values-dev.yaml")
            ]
            println helmDeployJob
        }

        stage ('deploy-prod') {
            def helmDeployJob = build job: 'helm-deploy-v1',
            wait: true,
            propagate: true,
            parameters: [
                string(name: 'K8S_CLUSTER', value: "us-east"),
                string(name: 'K8S_NAMESPACE', value: "prod"),
                string(name: 'CHART_NAME', value: "chartmuseum/divolte"),
                string(name: 'HELM_RELEASE_NAME', value: "divolte"),
                string(name: 'GITHUB_FILE_LIST', value: "charts/divolte/values-prod.yaml")
            ]
            println helmDeployJob
        }
    }
}
