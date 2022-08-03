Pod::Spec.new do |spec|
    spec.name                     = 'WifiEapConfigurator'
    spec.version                  = '1.0'
    spec.homepage                 = 'https://www.egeniq.com/'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'The wifi access point configuration library used iOS.'
    
    spec.platform                 = :ios, "13.0"
    spec.swift_version            = "5.0"

    spec.vendored_frameworks      = "build/cocoapods/framework/WifiEapConfigurator.framework"
    spec.libraries                = "c++"
    spec.module_name              = "#{spec.name}_umbrella"
    spec.source_files             = "*.{h,m,swift}"
    spec.public_header_files      = "*.h"
end
