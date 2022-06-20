from sys import argv, exit
from os import mkdir, makedirs, walk, path
from os.path import join, exists
from shutil import rmtree, copyfile, make_archive, copytree
import zipfile

connector_name = 'LaunchConnector'
connector_version = '1.0'
request_mapping = connector_name + '/' + connector_version

if argv[1] in ('-h', '-?'):
    print("USAGE: python build-deploy.py [-h|-?] <TargetConnectorName> optional<TargetConnectorVersion>")
    exit(0)

if len(argv) < 2:
    print("Invalid number of args.")
    print("USAGE: python build-deploy.py [-h|-?] <TargetConnectorName> optional<TargetConnectorVersion>")
    exit(1)

target_name = argv[1]
try:
    target_version = argv[2]
except IndexError:
    target_version = connector_version

target = target_name + target_version
deploy_dir = join('deploy', target)
deploy_src = join(deploy_dir, 'src', 'main')
deploy_lib = join(deploy_dir, 'lib')
deploy_cert = join(deploy_dir, 'cert')

folders = [deploy_src, deploy_lib, deploy_cert]

if exists(deploy_dir):
    rmtree(deploy_dir)
makedirs(deploy_src)

# this method looks at the files in a directory and replaces the old connector name/version with target name/version
def copyreplace(dir_name):
    for root, dirs, files in walk(dir_name):
        this_directory = join(deploy_dir, root)
        if not exists(this_directory):
            mkdir(this_directory)
        for filename in files:
            src_filename = join(root, filename)
            dest_filename = join(deploy_dir, src_filename)
            if filename.endswith('.java') or filename.endswith('.json') or filename.endswith('.ftl'):
                with open(src_filename, 'r') as file_in:
                    data = file_in.read().replace(request_mapping, connector_name + "/" + target_version) \
                        .replace(connector_name, target_name)
                    with open(dest_filename, 'w') as file_out:
                        file_out.write(data)
            else:
                copyfile(src_filename, dest_filename)

# this method takes the files in a directory and adds them to the zip archive
def zipdir(dir_name, ziph):
    # print("inside zipdir " + dir_name)
    for root, dirs, files in walk(dir_name):
        # print("inside walkpath")
        for filename in files:
            src_filename = join(root, filename)
            dest_filename = path.relpath(path.join(root, filename), path.join(folders[0], '../../'))
            # print("inside file loop")
            # print("new " + src_filename + " " + dest_filename)
            ziph.write(src_filename, dest_filename)

# this method loops through a list of directories to add each one to the archive
def zipit(dir_list, zip_name):
    # print("inside zipit")
    deploy_folder = path.join("deploy", zip_name)
    zipf = zipfile.ZipFile(deploy_folder, 'w', zipfile.ZIP_DEFLATED)
    for dir in dir_list:
        zipdir(dir, zipf)
    zipf.close()


# this updates the docker file for this version with datadog apm
docker_destination = join(deploy_dir, 'Dockerfile')
with open('Dockerfile', 'r') as file_in:
    docker_data = file_in.read().replace('{serviceName}', target_name).replace('{serviceVersion}', target_version)
    with open(docker_destination, 'w') as file_out:
        file_out.write(docker_data)

# copy the pom file, cert and lib directories to the deploy folder
copyfile('pom.xml', join(deploy_dir, 'pom.xml'))
if exists('cert'):
    copytree('cert', join(deploy_dir, 'cert'))

if exists('lib'):
    copytree('lib', join(deploy_dir, 'lib'))
copyreplace('src/main')



zipit(folders, join(target, "externalconnector.zip"))

# delete the copied folders from the deploy directory
rmtree(join(deploy_dir, "src"))
if exists('lib'):
    rmtree(deploy_lib)
if exists('cert'):
    rmtree(deploy_cert)

