import com.amazonaws.services.ec2.model.InstanceType
import hudson.model.*
import hudson.plugins.ec2.*
import jenkins.model.Jenkins

class Templates {

    private String securityGroup
    private String subnetId
    private String iamProfile
    private String zone

    Templates() {
        println("Empty Contructor")
    }

    Templates(String securityGroup, String subnetId, String iamProfile, String zone) {
        this.securityGroup = securityGroup
        this.subnetId = subnetId
        this.iamProfile = iamProfile
        this.zone = zone
    }

    public Map baseTemplate() {
        return [
            ami: 'ami-AAAAAAAA',            // String
            zone: this.zone,                     // String
            spotConfig: null,               // SpotConfiguration
            securityGroups: this.securityGroup,  // String
            remoteFS: '',                   // String
            type: InstanceType.fromValue('c5.xlarge'), // InstanceType
            ebsOptimized: false,                       // boolean
            labelString: '',                           // String
            mode: Node.Mode.NORMAL,                    // Node.Mode
            description: '',        // String
            initScript: '',         // String
            tmpDir: '',             // String
            userData: '',           // String
            numExecutors: '1',      // String
            remoteAdmin: '',        // String
            amiType: null,          // AMITypeData
            jvmopts: '',            // String
            stopOnTerminate: true,  // boolean
            subnetId: this.subnetId,     // String
            tags: null,             // List<EC2Tag>
            idleTerminationMinutes: '20',   // String
            usePrivateDnsName: false,       // boolean
            instanceCapStr: '5',            // String
            iamInstanceProfile: this.iamProfile, // String
            deleteRootOnTermination: true,  // boolean
            useEphemeralDevices: false,     // boolean
            useDedicatedTenancy: false,     // boolean
            launchTimeoutStr: '1000',       // String
            associatePublicIp: false,       // boolean
            customDeviceMapping: '',        // String
            connectBySSHProcess: false,     // boolean
            connectUsingPublicIp: false     // boolean
        ]
    }

    public Map windowsTemplate(String ami, String name, String instanceType, String instanceCap) {
        Map windowsProps = baseTemplate()
        windowsProps.ami = ami
        windowsProps.remoteFS = 'C:\\Jenkins'
        windowsProps.amiType = new WindowsData(null, false, '5', false, false)
        windowsProps.tags = [new EC2Tag('Name', name)]
        windowsProps.description = 'Windows EC2 Machine'
        windowsProps.remoteAdmin = 'Administrator'
        windowsProps.type = InstanceType.fromValue(instanceType)
        windowsProps.instanceCapStr = instanceCap

        return windowsProps
    }

    public Map ubuntuTemplate(String ami, String name, String instanceType, String instanceCap) {
        Map ubuntuProps = baseTemplate()
        ubuntuProps.ami = ami
        ubuntuProps.remoteFS = '\\Jenkins'
        ubuntuProps.amiType = new UnixData(null, null, null, 22, null)
        ubuntuProps.tags = [new EC2Tag('Name', name)]
        ubuntuProps.description = 'Ubuntu Machine'
        ubuntuProps.remoteAdmin = 'ubuntu'
        ubuntuProps.type = InstanceType.fromValue(instanceType)
        ubuntuProps.instanceCapStr = instanceCap

        return windowsProps
    }

}

Templates templateCreator = new Templates(securityGroup: 'SecurityGroup', subnetId: 'MySubnet', iamProfile: 'MyProfile', zone: 'MyZone')

Map windowsTemplate = templateCreator.windowsTemplate(ami: 'AmiValue', name: 'MyWindows', instanceType: 'c5.2xlarge', instanceCap: '14')

println(windowsTemplate)

