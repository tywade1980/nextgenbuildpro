#!/usr/bin/env python3

import requests
import sys
import json
from datetime import datetime
from typing import Dict, Any

class VoiceOrchestratorAPITester:
    def __init__(self, base_url="https://voice-orchestrator-1.preview.emergentagent.com"):
        self.base_url = base_url
        self.api_url = f"{base_url}/api"
        self.tests_run = 0
        self.tests_passed = 0
        self.session_id = None
        self.agent_id = None
        self.skill_id = None
        self.call_id = None

    def run_test(self, name: str, method: str, endpoint: str, expected_status: int, 
                 data: Dict[Any, Any] = None, params: Dict[str, str] = None) -> tuple:
        """Run a single API test"""
        url = f"{self.api_url}/{endpoint.lstrip('/')}"
        headers = {'Content-Type': 'application/json'}

        self.tests_run += 1
        print(f"\n🔍 Testing {name}...")
        print(f"   URL: {url}")
        
        try:
            if method == 'GET':
                response = requests.get(url, headers=headers, params=params, timeout=30)
            elif method == 'POST':
                response = requests.post(url, json=data, headers=headers, params=params, timeout=30)
            elif method == 'PUT':
                response = requests.put(url, json=data, headers=headers, timeout=30)
            elif method == 'DELETE':
                response = requests.delete(url, headers=headers, timeout=30)

            success = response.status_code == expected_status
            if success:
                self.tests_passed += 1
                print(f"✅ Passed - Status: {response.status_code}")
                try:
                    response_data = response.json() if response.content else {}
                    if response_data:
                        print(f"   Response: {json.dumps(response_data, indent=2)[:200]}...")
                except:
                    response_data = {}
            else:
                print(f"❌ Failed - Expected {expected_status}, got {response.status_code}")
                try:
                    error_data = response.json() if response.content else {}
                    print(f"   Error: {error_data}")
                except:
                    print(f"   Raw response: {response.text[:200]}")
                response_data = {}

            return success, response_data

        except Exception as e:
            print(f"❌ Failed - Error: {str(e)}")
            return False, {}

    def test_root_endpoint(self):
        """Test basic API connectivity"""
        return self.run_test("Root API Endpoint", "GET", "/", 200)

    def test_stats_endpoint(self):
        """Test stats endpoint"""
        return self.run_test("Stats Endpoint", "GET", "/stats", 200)

    def test_conversation_flow(self):
        """Test complete conversation flow"""
        print("\n📝 Testing Conversation Flow...")
        
        # 1. Get existing sessions
        success, sessions = self.run_test("Get Sessions", "GET", "/sessions", 200)
        if not success:
            return False

        # 2. Create new session
        success, session = self.run_test("Create Session", "POST", "/sessions", 200)
        if success and session:
            self.session_id = session.get('id')
            print(f"   Created session: {self.session_id}")

        # 3. Send chat message
        if self.session_id:
            chat_data = {
                "content": "Hello ARIA, this is a test message.",
                "session_id": self.session_id
            }
            success, response = self.run_test("Send Chat Message", "POST", "/chat", 200, chat_data)
            if success:
                print(f"   AI Response: {response.get('content', '')[:100]}...")

        # 4. Get messages for session
        if self.session_id:
            success, messages = self.run_test("Get Messages", "GET", f"/messages/{self.session_id}", 200)
            if success:
                print(f"   Retrieved {len(messages)} messages")

        return True

    def test_agent_swarm(self):
        """Test agent swarm management"""
        print("\n🤖 Testing Agent Swarm...")
        
        # 1. Get existing agents
        success, agents = self.run_test("Get Agents", "GET", "/agents", 200)
        if not success:
            return False

        # 2. Create new agent
        agent_data = {
            "name": f"Test Agent {datetime.now().strftime('%H%M%S')}",
            "description": "Test agent for API testing",
            "model": "gpt-5.2",
            "system_prompt": "You are a test assistant.",
            "skills": [],
            "status": "inactive"
        }
        success, agent = self.run_test("Create Agent", "POST", "/agents", 200, agent_data)
        if success and agent:
            self.agent_id = agent.get('id')
            print(f"   Created agent: {self.agent_id}")

        # 3. Toggle agent status
        if self.agent_id:
            success, toggle_result = self.run_test("Toggle Agent", "POST", f"/agents/{self.agent_id}/toggle", 200)
            if success:
                print(f"   Agent status: {toggle_result.get('status')}")

        # 4. Get specific agent
        if self.agent_id:
            success, agent_details = self.run_test("Get Agent Details", "GET", f"/agents/{self.agent_id}", 200)

        # 5. Update agent
        if self.agent_id:
            update_data = {
                "name": "Updated Test Agent",
                "description": "Updated description",
                "model": "gpt-4o",
                "system_prompt": "You are an updated test assistant.",
                "skills": [],
                "status": "active"
            }
            success, updated_agent = self.run_test("Update Agent", "PUT", f"/agents/{self.agent_id}", 200, update_data)

        return True

    def test_skills_system(self):
        """Test skills/connectors system"""
        print("\n🔌 Testing Skills System...")
        
        # 1. Get existing skills
        success, skills = self.run_test("Get Skills", "GET", "/skills", 200)
        if not success:
            return False

        # 2. Create new skill
        skill_data = {
            "name": f"Test Skill {datetime.now().strftime('%H%M%S')}",
            "description": "Test skill for API testing",
            "category": "custom",
            "config": {"test": True}
        }
        success, skill = self.run_test("Create Skill", "POST", "/skills", 200, skill_data)
        if success and skill:
            self.skill_id = skill.get('id')
            print(f"   Created skill: {self.skill_id}")

        # 3. Toggle skill
        if self.skill_id:
            success, toggle_result = self.run_test("Toggle Skill", "PUT", f"/skills/{self.skill_id}/toggle", 200)
            if success:
                print(f"   Skill enabled: {toggle_result.get('enabled')}")

        return True

    def test_call_handling(self):
        """Test call handling placeholders"""
        print("\n📞 Testing Call Handling...")
        
        # 1. Get existing calls
        success, calls = self.run_test("Get Calls", "GET", "/calls", 200)
        if not success:
            return False

        # 2. Initiate call (placeholder)
        test_number = "+1234567890"
        success, call_result = self.run_test("Initiate Call", "POST", "/calls/initiate", 200, 
                                           params={"phone_number": test_number})
        if success and call_result:
            self.call_id = call_result.get('call_id')
            print(f"   Call initiated: {self.call_id}")
            print(f"   Message: {call_result.get('message')}")

        return True

    def test_settings(self):
        """Test settings management"""
        print("\n⚙️ Testing Settings...")
        
        # 1. Get current settings
        success, settings = self.run_test("Get Settings", "GET", "/settings", 200)
        if not success:
            return False

        # 2. Update settings
        settings_update = {
            "default_model": "gpt-5.2",
            "default_voice": "nova",
            "tts_speed": 1.2,
            "runpod_endpoint": "https://test.endpoint.com",
            "runpod_api_key": "test_key"
        }
        success, updated_settings = self.run_test("Update Settings", "PUT", "/settings", 200, settings_update)

        return True

    def test_tts_functionality(self):
        """Test TTS functionality"""
        print("\n🔊 Testing TTS...")
        
        tts_data = {
            "text": "Hello, this is a test of the text to speech system.",
            "voice": "nova",
            "speed": 1.0
        }
        
        # Test base64 TTS (easier to verify)
        success, tts_result = self.run_test("TTS Base64", "POST", "/tts/base64", 200, tts_data)
        if success and tts_result:
            audio_data = tts_result.get('audio', '')
            print(f"   Audio data length: {len(audio_data)} characters")
            print(f"   Format: {tts_result.get('format')}")

        return success

    def cleanup(self):
        """Clean up test data"""
        print("\n🧹 Cleaning up test data...")
        
        # Delete test session
        if self.session_id:
            self.run_test("Delete Session", "DELETE", f"/sessions/{self.session_id}", 200)

        # Delete test agent
        if self.agent_id:
            self.run_test("Delete Agent", "DELETE", f"/agents/{self.agent_id}", 200)

        # Delete test skill
        if self.skill_id:
            self.run_test("Delete Skill", "DELETE", f"/skills/{self.skill_id}", 200)

    def run_all_tests(self):
        """Run all API tests"""
        print("🚀 Starting Voice Orchestrator API Tests")
        print(f"Base URL: {self.base_url}")
        print("=" * 60)

        try:
            # Basic connectivity
            self.test_root_endpoint()
            self.test_stats_endpoint()

            # Core functionality
            self.test_conversation_flow()
            self.test_agent_swarm()
            self.test_skills_system()
            self.test_call_handling()
            self.test_settings()
            
            # TTS functionality
            self.test_tts_functionality()

        except KeyboardInterrupt:
            print("\n⚠️ Tests interrupted by user")
        except Exception as e:
            print(f"\n💥 Unexpected error: {e}")
        finally:
            # Always try to cleanup
            self.cleanup()

        # Print results
        print("\n" + "=" * 60)
        print(f"📊 Test Results: {self.tests_passed}/{self.tests_run} passed")
        
        if self.tests_passed == self.tests_run:
            print("🎉 All tests passed!")
            return 0
        else:
            print(f"❌ {self.tests_run - self.tests_passed} tests failed")
            return 1

def main():
    tester = VoiceOrchestratorAPITester()
    return tester.run_all_tests()

if __name__ == "__main__":
    sys.exit(main())