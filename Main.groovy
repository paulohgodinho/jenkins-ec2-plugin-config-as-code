/*** BEGIN META {
  "name" : "Configure EC2 Cloud - EC2 Plugin",
  "comment" : "Modify this script to setup your nodes, instead of using the UI",
  "parameters" : [],
  "core": "2.0",
  "authors" : [
    { name : "Paulo Godinho" }
  ]
} END META**/

import com.amazonaws.services.ec2.model.InstanceType
import hudson.model.*
import hudson.plugins.ec2.*
import jenkins.model.Jenkins

class Templates {

    private final String securityGroup
    private final String subnetId
    private final String iamProfile

    Templates() {
        println('Empty Contructor')
    }

    Templates(String securityGroup, String subnetId, String iamProfile) {
        this.securityGroup = securityGroup
        this.subnetId = subnetId
        this.iamProfile = iamProfile
    }

    public Map baseTemplate() {
        return [
            ami: 'ami-AAAAAAAA',                // String
            zone: '',                           // String
            spotConfig: null,                   // SpotConfiguration
            securityGroups: this.securityGroup, // String
            remoteFS: '',                       // String
            type: InstanceType.fromValue('c5.xlarge'), // InstanceType
            ebsOptimized: false,                       // boolean
            labelString: '',                           // String
            mode: Node.Mode.EXCLUSIVE,                 // Node.Mode NORMAL/EXCLUSIVE
            description: '',         // String
            initScript: '',          // String
            tmpDir: '',              // String
            userData: '',            // String
            numExecutors: '1',       // String
            remoteAdmin: '',         // String
            amiType: null,           // AMITypeData
            jvmopts: '',             // String
            stopOnTerminate: true,   // boolean
            subnetId: this.subnetId, // String
            tags: null,              // List<EC2Tag>
            idleTerminationMinutes: '20',        // String
            usePrivateDnsName: false,            // boolean
            instanceCapStr: '5',                 // String
            iamInstanceProfile: this.iamProfile, // String
            deleteRootOnTermination: true,       // boolean
            useEphemeralDevices: false,          // boolean
            useDedicatedTenancy: false,          // boolean
            launchTimeoutStr: '1000',            // String
            associatePublicIp: false,            // boolean
            customDeviceMapping: '',             // String
            connectBySSHProcess: false,          // boolean
            connectUsingPublicIp: false          // boolean
        ]
    }

    public SlaveTemplate getTemplateFromProps(Map props) {
        return new SlaveTemplate(
            props.ami,
            props.zone,
            props.spotConfig,
            props.securityGroups,
            props.remoteFS,
            props.type,
            props.ebsOptimized,
            props.labelString,
            props.mode,
            props.description,
            props.initScript,
            props.tmpDir,
            props.userData,
            props.numExecutors,
            props.remoteAdmin,
            props.amiType,
            props.jvmopts,
            props.stopOnTerminate,
            props.subnetId,
            props.tags,
            props.idleTerminationMinutes,
            props.usePrivateDnsName,
            props.instanceCapStr,
            props.iamInstanceProfile,
            props.useEphemeralDevices,
            props.useDedicatedTenancy,
            props.launchTimeoutStr,
            props.associatePublicIp,
            props.customDeviceMapping,
            props.connectBySSHProcess
        )
    }

    public SlaveTemplate windowsTemplate(String ami, String name, String label, String description, String instanceType, String instanceCap) {
        Map windowsProps = baseTemplate()
        windowsProps.ami = ami
        windowsProps.description = description
        windowsProps.labelString = label
        windowsProps.remoteFS = 'C:\\Jenkins'
        windowsProps.amiType = new WindowsData(null, false, '3', false, false)
        windowsProps.tags = [new EC2Tag('Name', name)]
        windowsProps.remoteAdmin = 'Administrator'
        windowsProps.type = InstanceType.fromValue(instanceType)
        windowsProps.instanceCapStr = instanceCap

        SlaveTemplate nodeTemplate = getTemplateFromProps(windowsProps)
        nodeTemplate.setHostKeyVerificationStrategy(HostKeyVerificationStrategyEnum.OFF)
        return nodeTemplate
    }

    public SlaveTemplate ubuntuTemplate(String ami, String name, String label, String description, String instanceType, String instanceCap) {
        Map ubuntuProps = baseTemplate()
        ubuntuProps.ami = ami
        ubuntuProps.description = description
        ubuntuProps.labelString = label
        ubuntuProps.remoteFS = '\\Jenkins'
        ubuntuProps.amiType = new UnixData(null, null, null, '22', null)
        ubuntuProps.tags = [new EC2Tag('Name', name)]
        ubuntuProps.remoteAdmin = 'ubuntu'
        ubuntuProps.type = InstanceType.fromValue(instanceType)
        ubuntuProps.instanceCapStr = instanceCap

        SlaveTemplate nodeTemplate = getTemplateFromProps(ubuntuProps)
        nodeTemplate.setHostKeyVerificationStrategy(HostKeyVerificationStrategyEnum.OFF)
        return nodeTemplate
    }

}

def nodes = []

// Cloud Settings
def amazonEC2CloudProps = [
  cloudName: 'AWSEC2',
  instanceCapStr: '15',
  region: 'us-east-2',
  useInstanceProfileForCredentials: true,
  sshKeysCredentialsId: 'tooling/jenkins/privatekey',
  privateKey: '',
  credentialsId:  '',
]

// Base Settings Shared by All Machines
Templates templateHelper = new Templates(
    'sg-0d2378908402832',
    'subnet-0c5a259319a43657a',
    'arn:aws:iam::321762876544:instance-profile/JenkinsManagedInstancesRole')

// Define your nodes here

// Example Windows Node
SlaveTemplate windowsNode = templateHelper.windowsTemplate(
    'ami-018ds2126c6b26205',
    '[Jenkins-Managed] Windows-UnrealGameBuilder 4.26',
    'unreal-4.26',
    'Machine Used to build with Unreal 4.26',
    'c5.2xlarge',
    '10'
)
nodes.add(windowsNode)

// Example Ubuntu Node
SlaveTemplate ubuntuNode = templateHelper.ubuntuTemplate(
    'ami-09a57ecfg964e2efb',
    '[Jenkins-Managed] Ubuntu20-With-Tools',
    'ubuntu-with-tools',
    'General purpose Ubuntu machine',
    'c5.xlarge',
    '10'
)
nodes.add(ubuntuNode)

AmazonEC2Cloud amazonEC2Cloud = new AmazonEC2Cloud(
  amazonEC2CloudProps.cloudName,
  amazonEC2CloudProps.useInstanceProfileForCredentials,
  amazonEC2CloudProps.credentialsId,
  amazonEC2CloudProps.region,
  amazonEC2CloudProps.privateKey,
  amazonEC2CloudProps.sshKeysCredentialsId,
  amazonEC2CloudProps.instanceCapStr,
  nodes,
  '',
  ''
)

// Clean all Clouds
Jenkins jenkins = Jenkins.getInstance()
jenkins.clouds.clear()
jenkins.save()

// Add new one
jenkins.clouds.add(amazonEC2Cloud)
jenkins.save()