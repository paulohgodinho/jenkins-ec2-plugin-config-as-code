import Templates

Templates templateCreator = new Templates(securityGroup: 'SecurityGroup', subnetId: 'MySubnet', iamProfile: 'MyProfile', zone: 'MyZone')

Map windowsTemplate = templateCreator.windowsTemplate(ami: 'AmiValue', name: 'MyWindows', instanceType: 'c5.2xlarge', instanceCap: '14')

println(windowsTemplate)
