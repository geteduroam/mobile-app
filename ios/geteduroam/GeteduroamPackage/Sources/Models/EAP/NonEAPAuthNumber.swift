import Foundation
import XMLCoder

public enum NonEAPAuthNumber: Int, Codable, Equatable {
    case PAP = 1
    case MSCHAP = 2
    case MSCHAPv2 = 3
}
